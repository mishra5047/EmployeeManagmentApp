package com.abhishek.employeemanagment.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
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
import java.util.*
import kotlin.concurrent.schedule

class HomeFragment : Fragment() {

    // binding variables
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // context variable used throughout the fragment
    private lateinit var contextVar: Context

    // context variable used throughout the fragment
    private lateinit var viewModelProviderFactoryViewModelFactory: HomeFragmentViewModelFactory
    private lateinit var viewModel: HomeFragmentViewModel

    // list of employees adapter variable
    private lateinit var employeeListAdapter: EmployeeListAdapter

    // variable to handle logic for existing employee id's
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

    /**
     * function to instantiate the UI
     */
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

    /**
     * function to attach click listeners on the UI elements
     */
    private fun attachClickListeners() {

        binding.apply {
            // button for adding an employee
            addEmployeeButton.setOnClickListener {
                when {
                    contextVar.isConnected() -> {
                        // opening the fragment if internet is connected
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

            // button for syncing the employee list from API
            iconSync.setOnClickListener {
                when {
                    contextVar.isConnected() -> {
                        // get the list of employees from the API
                        setOfEmployeesSelected = HashSet()
                        viewModel.getAllEmployeesOnline()
                    }
                    else -> {
                        contextVar.toastyError(getString(R.string.no_internet))
                    }
                }
            }

            // button for deleting selected employees
            iconDelete.setOnClickListener {
                if (setOfEmployeesSelected.size == 0) {
                    contextVar.toastyInfo("No Employees Selected")
                    return@setOnClickListener
                }

                when {
                    contextVar.isConnected() -> {

                        // display alertdialog to confirm deletion
                        val builder: AlertDialog.Builder = AlertDialog.Builder(contextVar)
                        builder.setMessage("Do you want to delete these employees ?")
                        builder.setTitle("Alert !")
                        builder.setCancelable(false)
                        builder
                            .setPositiveButton(
                                "Yes"
                            ) { dialog, which ->

                                if(setOfEmployeesSelected.size == 1){
                                    for(id in setOfEmployeesSelected) {
                                        viewModel.deleteOneEmployee(id)
                                    }
                                }else{
                                    //delete multiple employees
                                    viewModel.deleteMultipleEmployees(setOfEmployeesSelected)
                                }
                            }
                        builder
                            .setNegativeButton(
                                "No"
                            ) { dialog, which ->
                                //dismiss the dialog box
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
    }

    /**
     * function to getEmployees list depending on the internet connection
     * If Connected -> Get from API
     * Else -> Get From Local Storage
     */
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

    /**
     * function to observe livedata variables in the view model
     */
    private fun observeViewModel() {

        // variables for getting employees from the local database
        viewModel.employeesGetterOffline.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    is Resource.Loading -> {
                        //request is being processed
                        progressBarHomeFragment.isVisible = true
                        employeeRecyclerView.isGone = true
                        imgErrorDb.isGone = true
                        noEmployeesParentLayout.isGone = true
                    }
                    is Resource.Error -> {
                        //request has resulted in and error
                        progressBarHomeFragment.isGone = true
                        employeeRecyclerView.isGone = true
                        noEmployeesParentLayout.isGone = true
                        imgErrorDb.isVisible = true
                    }
                    is Resource.Success -> {
                        //all employees fetched
                        progressBarHomeFragment.isGone = true
                        employeeRecyclerView.isVisible = true
                        imgErrorDb.isGone = true
                        setEmployeesListOnUI(it.data)
                    }
                }
            }
        }

        // variable for getting the list of employees from the API
        viewModel.employeesGetterOnline.observe(viewLifecycleOwner) { it ->
            binding.apply {
                when (it) {
                    is Resource.Loading -> {
                        //request is still loading
                        progressBarHomeFragment.isVisible = true
                        employeeRecyclerView.isGone = true
                        imgErrorDb.isGone = true
                        noEmployeesParentLayout.isGone = true
                    }
                    is Resource.Error -> {
                        //request has given an error
                        progressBarHomeFragment.isGone = true
                        employeeRecyclerView.isGone = true
                        noEmployeesParentLayout.isGone = true
                        imgErrorDb.isVisible = true
                    }
                    is Resource.Success -> {
                        //list of employees fetched
                        //putting the new result from API in local DB
                        updateEmployeesInRoomDB(it.data)
                        //deleting all the images of the already existing employees
                    }
                }
            }
        }

        //variable to observe delete of an employees
        viewModel.deleteEmployeesGetter.observe(viewLifecycleOwner) { it ->
            when (it) {
                is Resource.Loading -> {
                    //request is being processed
                    binding.progressBarHomeFragment.isVisible = true
                }
                is Resource.Error -> {
                    //request has resulted in and error
                    binding.progressBarHomeFragment.isGone = true
                    contextVar.toastyError(it.data.toString())
                }
                is Resource.Success -> {
                    //employees deleted successfully
                    binding.progressBarHomeFragment.isGone = true
                    contextVar.toastySuccess("Employees Deleted")
                    Timer().schedule(1000) {
                        viewModel.getAllEmployeesOnline()
                    }
                }
            }
        }
    }

    /**
     * function to update the list of employees received from API call
     */
    private fun updateEmployeesInRoomDB(listOfEmployees: List<EmployeeClassAPIResponse>?) {

        // the received list is empty, clear the local database
        if (listOfEmployees == null || listOfEmployees.isEmpty()) viewModel.deleteAllEmployeesOffline()
        // if not null / empty. Update the local database
        listOfEmployees?.let {
            viewModel.updateRoomDBTable(listOfEmployees)
        }
    }

    /**
     * setting the list of employees on the UI.
     */
    private fun setEmployeesListOnUI(listOfEmployees: List<EmployeeEntity>?) {

        // the list is empty, displaying the empty DB image
        if (listOfEmployees == null || listOfEmployees.isEmpty()) {
            binding.apply {
                imgErrorDb.isGone = true
                noEmployeesParentLayout.isVisible = true
                employeeRecyclerView.isGone = true
            }
        } else {
            // preparing string of employee id to pass to add fragment
            for (employeeEntity in listOfEmployees) {
                listOfEmployeeIdString += "" + employeeEntity.id + ","
            }

            // adapter variable for the recycler view
            employeeListAdapter = EmployeeListAdapter(listOfEmployees, {
                // click item received from the recycler view

                // bundle object for the next fragment
                val bundle = bundleOf(
                    "id" to it.id,
                    "employeeName" to it.name,
                    "employeeDesignation" to it.designation,
                    "employeePicURL" to it.profilePicUrl
                )
                // triggering the navigation action
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_home_fragment_to_epmloyee_details_fragment, bundle)
            }) {
                // the click listener used to handle the employee selected action
                val positionOfItemClicked = it[0]
                val isCheckedVar = it[1]

                // the employee has been unchecked
                if (isCheckedVar == 0) setOfEmployeesSelected.remove(listOfEmployees[positionOfItemClicked].id)

                // the employee has been checked
                else setOfEmployeesSelected.add(listOfEmployees[positionOfItemClicked].id)
            }

            // setting the adapter and layout manager
            binding.employeeRecyclerView.apply {
                adapter = employeeListAdapter
                layoutManager = LinearLayoutManager(contextVar)
            }
        }
    }

    /**
     * nulling the binding to handle memory leaks.
     * Reference - https://medium.com/default-to-open/handling-lifecycle-with-view-binding-in-fragments-a7f237c56832
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
