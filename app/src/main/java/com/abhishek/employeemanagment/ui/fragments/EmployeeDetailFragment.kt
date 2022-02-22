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

    // binding variables
    private var _binding: FragmentEmployeeDetailsBinding? = null
    private val binding get() = _binding!!

    // context variable used throughout the fragment
    private lateinit var contextVar: Context

    // viewmodel variable
    private lateinit var viewModelProviderFactoryViewModelFactory: EmployeeDetailFragmentViewModelFactory
    private lateinit var viewModel: EmployeeDetailFragmentViewModel

    // global var of the employee being added
    private lateinit var employeeObject: EmployeeEntity

    // URI of the image selected from the gallery (if any)
    private var dataOfImageSelected: Uri? = null

    companion object {
        private const val STORAGE_PERMISSION_CODE = 101
    }

    /**
     * Callback function to instantiate binding
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEmployeeDetailsBinding.inflate(inflater)
        return _binding!!.root
    }

    /**
     * Function used for rendering the UI
     */
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

    /**
     * function to set click listeners for elements in UI
     */
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

                        cardViewImage.setOnClickListener {
                            if (ActivityCompat.checkSelfPermission(
                                    contextVar,
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                // ask permission
                                activity?.let { it1 ->
                                    ActivityCompat.requestPermissions(
                                        it1,
                                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                                        STORAGE_PERMISSION_CODE
                                    )
                                }
                            } else {
                                // permission granted
                                startGalleryToPickImage()
                            }
                        }
                    }
                    else -> {
                        contextVar.toastyError("Kindly Connect to internet to edit an employee")
                    }
                }
            }

            // gallery opener image view
            imageviewBack.setOnClickListener {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_employee_details_fragment_to_home_fragment)
            }

            // save details button
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
                        saveDetailsButton.isClickable = false
                        editDetailsButton.isGone = true
                        saveDetailsButton.isClickable = false
                        saveDetailsButton.isGone = true
                        deleteEmployeeButton.isClickable = false
                        deleteEmployeeButton.isGone = true
                        progressBarFragment.isVisible = true
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
                                editDetailsButton.isClickable = false
                                editDetailsButton.isGone = true
                                saveDetailsButton.isClickable = false
                                saveDetailsButton.isGone = true
                                deleteEmployeeButton.isClickable = false
                                deleteEmployeeButton.isGone = true
                                progressBarFragment.isVisible = true
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

    /**
     * function to delete the employee's image from firebase storage when employee deleted
     */
    private fun deleteImageFromFirebaseStorage(profilePicUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(profilePicUrl)
        reference.delete().addOnCompleteListener {
            Log.d("TAG", "Image Deleted From Storage")
        }
    }

    /**
     * function to upload the selected image to firebase using the URI of the image selected
     */
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

    /**
     * function to check whether the permission was granted or not in the permission asking dialog box
     */
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

    /**
     * function to open the device's gallery so that user can select an image
     */
    private fun startGalleryToPickImage() {
        val cameraIntent =
            Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        cameraIntent.type = "image/*"
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(cameraIntent, 1000)
        }
    }

/**
 * function to get the image selected in the gallery
 */
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
                    binding.textDisplayAddImage.isGone = true
                    //rendering the selected image to the imageview
                    binding.imageViewPerson.setImageBitmap(bitmapImage)
                }
            }
        }
    }

    /**
     * function to observe livedata variables in the view model
     */
    private fun observeViewModel() {
        // variable for deleting
        viewModel.deleteAnEmployeeGetter.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    // the request is being processed
                    contextVar.toastyInfo("Processing Request")
                }
                is Resource.Error -> {
                    // the request resulted in an error
                    binding.progressBarFragment.isGone = true
                    binding.deleteEmployeeButton.isVisible = true
                    contextVar.toastyError(it.data.toString())
                }
                is Resource.Success -> {
                    // employee added successfully
                    contextVar.toastySuccess("Employee Deleted")
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        binding.progressBarFragment.isGone = true
                        Navigation.findNavController(requireView())
                            .navigate(R.id.action_employee_details_fragment_to_home_fragment)
                    }
                }
            }
        }

        // variable for updating an employee
        viewModel.updateAnEmployeeGetter.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    // employee added successfully
                    contextVar.toastyInfo("Processing Request")
                }
                is Resource.Error -> {
                    // the request resulted in an error
                    contextVar.toastyError(it.data.toString())
                }
                is Resource.Success -> {
                    // employee deleted successfully
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

    /**
     * function to initialise the viewmodel factory and
     */
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

    /**
     * nulling the binding to handle memory leaks.
     * Reference - https://medium.com/default-to-open/handling-lifecycle-with-view-binding-in-fragments-a7f237c56832
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
