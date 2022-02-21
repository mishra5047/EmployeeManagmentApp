package com.abhishek.employeemanagment.ui.fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import coil.load
import com.abhishek.employeemanagment.R
import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.data.repository.EmployeeDetailRepository
import com.abhishek.employeemanagment.databinding.FragmentEmployeeDetailsBinding
import com.abhishek.employeemanagment.util.*
import com.abhishek.employeemanagment.viewmodel.EmployeeDetailFragmentViewModel
import com.abhishek.employeemanagment.viewmodel.factory.EmployeeDetailFragmentViewModelFactory
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EmployeeDetailFragment : Fragment() {

    private var _binding: FragmentEmployeeDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var contextVar: Context
    private lateinit var viewModelProviderFactoryViewModelFactory: EmployeeDetailFragmentViewModelFactory
    private lateinit var viewModel: EmployeeDetailFragmentViewModel
    private lateinit var employeeObject: EmployeeEntity
    private var dataOfImageSelected: Uri? = null

    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEmployeeDetailsBinding.inflate(inflater)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            employeeObject =
                EmployeeEntity(
                    it.get("id").toString().toInt(),
                    it.get("employeeName").toString(),
                    it.get("employeeDesignation").toString(),
                    it.get("employeePicURL").toString()
                )
        }
        contextVar = requireContext()
        initializeViewModel()
        setUI()
        observeViewModel()
    }

    private fun setUI() {
        binding.apply {
            imageViewPerson.load(employeeObject.profilePicUrl)
            nameOfEmployee.text = employeeObject.name
            designationOfEmployee.text = employeeObject.designation

            editDetailsButton.setOnClickListener {
                when {
                    contextVar.isConnected() -> {
                        contextVar.toastyInfo("Click on the imageview to select image from gallery")
                        employeesDetailDisplayLayout.isGone = true
                        saveDetailsButton.isVisible = true
                        editDetailsButton.isGone = true
                        editEmployeeDetailsLayout.isVisible = true
                        textFieldEmployeeName.editText?.setText(employeeObject.name)
                        textFieldEmployeeDesignation.editText?.setText(employeeObject.designation)

                        imageViewPerson.setOnClickListener {
                            if (ActivityCompat.checkSelfPermission(
                                    contextVar,
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                activity?.let { it1 ->
                                    ActivityCompat.requestPermissions(
                                        it1,
                                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                                        STORAGE_PERMISSION_CODE
                                    )
                                }
                            } else {
                                startGalleryToPickImage()
                            }
                        }
                    }
                    else -> {
                        contextVar.toastyError("Kindly Connect to internet to edit an employee")
                    }
                }
            }

            imageviewBack.setOnClickListener {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_employee_details_fragment_to_home_fragment)
            }

            saveDetailsButton.setOnClickListener {
                when {
                    contextVar.isConnected() -> {
                        // validate the enter details first
                        val employeeName = textFieldEmployeeName.editText?.text.toString()
                        val employeeDesignation =
                            textFieldEmployeeDesignation.editText?.text.toString()

                        if (employeeName.isEmpty() || employeeDesignation.isEmpty()) {
                            contextVar.toastyError("Employee Details Can't be Null")
                            return@setOnClickListener
                        }
                        employeeObject = EmployeeEntity(
                            employeeObject.id,
                            employeeName,
                            employeeDesignation,
                            employeeObject.profilePicUrl
                        )
                        if (dataOfImageSelected == null) {
                            contextVar.toastyInfo("Image Not Selected, Using the old image itself")
                            viewModel.updateAnEmployeeOnline(employeeObject)
                        } else {
                            uploadImageToFirebase()
                        }
                    }
                    else -> {
                        contextVar.toastyError("Kindly Connect to internet to update an employee")
                    }
                }
            }

            deleteEmployeeButton.setOnClickListener {
                when {
                    contextVar.isConnected() -> {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(contextVar)
                        builder.setMessage("Do you want to delete the employee ?")
                        builder.setTitle("Alert !")
                        builder.setCancelable(false)
                        builder
                            .setPositiveButton(
                                "Yes"
                            ) { dialog, which ->
                                viewModel.deleteAnEmployeeOnline(employeeObject.id)
                                deleteImageFromFirebaseStorage(employeeObject.profilePicUrl)
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
                        contextVar.toastyError("Kindly Connect to internet to delete an employee")
                    }
                }
            }
        }
    }

    private fun deleteImageFromFirebaseStorage(profilePicUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(profilePicUrl)
        reference.delete().addOnCompleteListener {
            Log.d("TAG", "Image Deleted From Storage")
        }
    }

    private fun uploadImageToFirebase() {
        dataOfImageSelected?.let { it ->
            val storageRef =
                FirebaseStorage.getInstance().reference.child(employeeObject.id.toString())
            val uploadTask = storageRef.putFile(it)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    employeeObject.profilePicUrl = downloadUri.toString()
                    viewModel.updateAnEmployeeOnline(employeeObject)
                } else {
                    // Handle failures
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGalleryToPickImage()
            } else {
                contextVar.toastyError("Kindly give storage permission to pick image from gallery")
            }
        }
    }

    private fun startGalleryToPickImage() {
        val cameraIntent =
            Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        cameraIntent.type = "image/*"
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(cameraIntent, 1000)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1000) {
                val returnUri = data?.data
                returnUri?.let {
                    dataOfImageSelected = returnUri
                    val bitmapImage =
                        MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            returnUri
                        )
                    binding.imageViewPerson.setImageBitmap(bitmapImage)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.deleteAnEmployeeGetter.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    contextVar.toastyInfo("Processing Request")
                }
                is Resource.Error -> {
                    contextVar.toastyError(it.data.toString())
                }
                is Resource.Success -> {
                    contextVar.toastySuccess("Employee Deleted")
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_employee_details_fragment_to_home_fragment)
                    }
                }
            }
        }

        viewModel.updateAnEmployeeGetter.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    contextVar.toastyInfo("Processing Request")
                }
                is Resource.Error -> {
                    contextVar.toastyError(it.data.toString())
                }
                is Resource.Success -> {
                    contextVar.toastySuccess("Employee Details Updated")
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(2000)
                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_employee_details_fragment_to_home_fragment)
                    }
                }
            }
        }
    }

    private fun initializeViewModel() {
        contextVar = requireContext()
        viewModelProviderFactoryViewModelFactory =
            EmployeeDetailFragmentViewModelFactory(
                EmployeeDetailRepository()
            )
        viewModel = ViewModelProvider(this, viewModelProviderFactoryViewModelFactory).get(
            EmployeeDetailFragmentViewModel::class.java
        )
    }
}
