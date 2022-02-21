package com.abhishek.employeemanagment.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.abhishek.employeemanagment.R
import com.abhishek.employeemanagment.data.local.EmployeeDatabase
import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.data.remote.EmployeeClassAPIResponse
import com.abhishek.employeemanagment.data.repository.HomeFragmentRepository
import com.abhishek.employeemanagment.databinding.FragmentHomeBinding
import com.abhishek.employeemanagment.ui.adapters.EmployeeListAdapter
import com.abhishek.employeemanagment.util.*
import com.abhishek.employeemanagment.viewmodel.HomeFragmentViewModel
import com.abhishek.employeemanagment.viewmodel.factory.HomeFragmentViewModelFactory
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.concurrent.schedule

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var contextVar: Context
    private lateinit var viewModelProviderFactoryViewModelFactory: HomeFragmentViewModelFactory
    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var employeeListAdapter: EmployeeListAdapter
    private var listOfEmployeeIdString = ""
    private var setOfEmployeesSelected = HashSet<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserInterface()
    }

    private fun setUserInterface() {
        contextVar = requireContext()
        viewModelProviderFactoryViewModelFactory =
            HomeFragmentViewModelFactory(
                HomeFragmentRepository(EmployeeDatabase(contextVar))
            )
        viewModel = ViewModelProvider(this, viewModelProviderFactoryViewModelFactory).get(
            HomeFragmentViewModel::class.java
        )
        getEmployeesList()
        observeViewModel()
        attachClickListeners()
    }

    private fun attachClickListeners() {
        binding.addEmployeeButton.setOnClickListener {
            when {
                contextVar.isConnected() -> {
                    val bundle = bundleOf(
                        "idString" to listOfEmployeeIdString
                    )
                    Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_fragment_to_employee_add_fragment, bundle)
                }
                else -> {
                    contextVar.toastyError(getString(R.string.no_internet))
                }
            }
        }

        binding.iconSync.setOnClickListener {
            when {
                contextVar.isConnected() -> {
                    viewModel.getAllEmployeesOnline()
                }
                else -> {
                    contextVar.toastyError(getString(R.string.no_internet))
                }
            }
        }

        binding.iconDelete.setOnClickListener {
            if (setOfEmployeesSelected.size == 0) {
                contextVar.toastyInfo("No Employees Selected")
                return@setOnClickListener
            }

            when {
                contextVar.isConnected() -> {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(contextVar)
                    builder.setMessage("Do you want to delete these employees ?")
                    builder.setTitle("Alert !")
                    builder.setCancelable(false)
                    builder
                        .setPositiveButton(
                            "Yes"
                        ) { dialog, which ->
                            viewModel.deleteMultipleEmployees(setOfEmployeesSelected)
                        }
                    builder
                        .setNegativeButton(
                            "No"
                        ) { dialog, which ->
                            dialog.cancel()
                        }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()
                }
                else -> {
                    contextVar.toastyError(getString(R.string.no_internet))
                }
            }
        }
    }

    private fun deleteImageFromFirebaseStorage(employeeIdSet: HashSet<Int>) {

        for(employeeId in employeeIdSet){
            val reference = FirebaseStorage.getInstance().getReference("" + employeeId)
            reference.delete().addOnCompleteListener {
                Log.d("TAG", "Image Deleted From Storage")
            }
        }
    }

    private fun getEmployeesList() {
        when {
            contextVar.isConnected() -> {
                viewModel.getAllEmployeesOnline()
            }
            else -> {
                contextVar.toastyInfo(getString(R.string.local_data_string))
                viewModel.getAllEmployeesOffline()
            }
        }
    }

    private fun observeViewModel() {

        viewModel.employeesGetterOffline.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    is Resource.Loading -> {
                        //progress bar
                        progressBarHomeFragment.isVisible = true
                        employeeRecyclerView.isGone = true
                        imgErrorDb.isGone = true
                        noEmployeesParentLayout.isGone = true
                    }
                    is Resource.Error -> {
                        progressBarHomeFragment.isGone = true
                        employeeRecyclerView.isGone = true
                        noEmployeesParentLayout.isGone = true
                        imgErrorDb.isVisible = true
                    }
                    is Resource.Success -> {
                        //hide progress bar
                        progressBarHomeFragment.isGone = true
                        employeeRecyclerView.isVisible = true
                        imgErrorDb.isGone = true
                        setEmployeesListOnUI(it.data)
                    }
                }
            }
        }

        viewModel.employeesGetterOnline.observe(viewLifecycleOwner) { it ->
            binding.apply {
                when (it) {
                    is Resource.Loading -> {
                        //progress bar
                        progressBarHomeFragment.isVisible = true
                        employeeRecyclerView.isGone = true
                        imgErrorDb.isGone = true
                        noEmployeesParentLayout.isGone = true
                    }
                    is Resource.Error -> {
                        //displaying error layout
                        progressBarHomeFragment.isGone = true
                        employeeRecyclerView.isGone = true
                        noEmployeesParentLayout.isGone = true
                        imgErrorDb.isVisible = true
                    }
                    is Resource.Success -> {
                        //hide progress bar
                        updateEmployeesInRoomDB(it.data)
                        deleteImageFromFirebaseStorage(setOfEmployeesSelected)
                    }
                }
            }
        }

        viewModel.deleteEmployeesGetter.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {
                    //progress bar
                    binding.progressBarHomeFragment.isVisible = true
                }
                is Resource.Error -> {
                    //displaying error layout
                    binding.progressBarHomeFragment.isGone = true
                    contextVar.toastyError(it.data.toString())
                }
                is Resource.Success -> {
                    //hide progress bar
                    binding.progressBarHomeFragment.isGone = true
                    contextVar.toastySuccess("Employees Deleted")
                    Timer().schedule(1000) {
                        viewModel.getAllEmployeesOnline()
                    }
                }
            }
        }
    }

    private fun updateEmployeesInRoomDB(data: List<EmployeeClassAPIResponse>?) {
        if (data == null || data.isEmpty()) viewModel.deleteAllEmployeesOffline()
        data?.let {
            viewModel.updateRoomDBTable(data)
        }
    }

    private fun setEmployeesListOnUI(data: List<EmployeeEntity>?) {
        if (data == null || data.isEmpty()) {
            binding.apply {
                imgErrorDb.isGone = true
                noEmployeesParentLayout.isVisible = true
                employeeRecyclerView.isGone = true
            }
        } else {
            // preparing string of employee id to pass to add fragment
            for (employeeEntity in data) {
                listOfEmployeeIdString += "" + employeeEntity.id + ","
            }

            employeeListAdapter = EmployeeListAdapter(data, {
                // click item received
                val bundle = bundleOf(
                    "id" to it.id,
                    "employeeName" to it.name,
                    "employeeDesignation" to it.designation,
                    "employeePicURL" to it.profilePicUrl
                )
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_home_fragment_to_epmloyee_details_fragment, bundle)
            }) {
                val positionOfItemClicked = it[0]
                val isCheckedVar = it[1]

                if (isCheckedVar == 0) setOfEmployeesSelected.remove(data[positionOfItemClicked].id)
                else setOfEmployeesSelected.add(data[positionOfItemClicked].id)
            }
            binding.employeeRecyclerView.apply {
                adapter = employeeListAdapter
                layoutManager = LinearLayoutManager(contextVar)
            }
        }
    }

    // to handle memory leaks
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
