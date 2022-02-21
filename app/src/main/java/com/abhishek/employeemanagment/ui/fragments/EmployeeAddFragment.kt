package com.abhishek.employeemanagment.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.abhishek.employeemanagment.R
import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.data.repository.EmployeeAddRepository
import com.abhishek.employeemanagment.databinding.FragmentAddEmployeeBinding
import com.abhishek.employeemanagment.util.Resource
import com.abhishek.employeemanagment.util.toastyError
import com.abhishek.employeemanagment.util.toastyInfo
import com.abhishek.employeemanagment.util.toastySuccess
import com.abhishek.employeemanagment.viewmodel.EmployeeAddFragmentViewModel
import com.abhishek.employeemanagment.viewmodel.factory.EmployeeAddFragmentViewModelFactory
import com.google.firebase.storage.FirebaseStorage

class EmployeeAddFragment : Fragment() {

    private var _binding: FragmentAddEmployeeBinding? = null
    private val binding get() = _binding!!
    private lateinit var contextVar: Context
    private lateinit var viewModelProviderFactoryViewModelFactory: EmployeeAddFragmentViewModelFactory
    private lateinit var viewModel: EmployeeAddFragmentViewModel
    private lateinit var employeeObject: EmployeeEntity
    private var dataOfImageSelected: Uri? = null
    private lateinit var listOfEmployeeIds: String
    private lateinit var setOfEmployeeIds: HashSet<Int>

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
        _binding = FragmentAddEmployeeBinding.inflate(inflater)
        return _binding!!.root
    }

    /**
     * Function used for rendering the UI
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contextVar = requireContext()
        setOfEmployeeIds = HashSet()
        listOfEmployeeIds = String()
        arguments?.let {
            if (it.containsKey("idString")) {
                listOfEmployeeIds = it.get("idString").toString()
                prepareEmployeeSet()
            }
        }
        initializeViewModel()
        setOnClickListeners()
        observeViewModel()
    }

    /**
     * Function to prepare the employee set to check for duplicate employee id
     */
    private fun prepareEmployeeSet() {
        if (listOfEmployeeIds.isEmpty()) return
        listOfEmployeeIds.split(",").map {
            if (it != "")
                setOfEmployeeIds.add(it.toInt())
        }
    }

    /**
     * function to observe livedata variables in the view model
     */
    private fun observeViewModel() {
        viewModel.addAnEmployeeGetter.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    binding.progressBarFragment.isGone = true
                    binding.saveDetailsButton.isVisible = true
                    contextVar.toastyError(it.data.toString())
                }
                is Resource.Success -> {
                    contextVar.toastySuccess("Employee Added")
                    Navigation.findNavController(requireView())
                        .navigate(R.id.action_employee_add_fragment_to_home_fragment)
                }
            }
        }
    }

    /**
     * function to set click listeners for elements in UI
     */
    private fun setOnClickListeners() {
        binding.apply {

            // click listener for imageview that's used to display
            cardViewImage.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(
                        contextVar,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // permission not granted, ask permission
                    activity?.let { it1 ->
                        ActivityCompat.requestPermissions(
                            it1,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            STORAGE_PERMISSION_CODE
                        )
                    }
                } else {
                    // permission granted open gallery
                    startGalleryToPickImage()
                }
            }

            // click listener for back button image
            imageviewBack.setOnClickListener {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_employee_details_fragment_to_home_fragment)
            }

            // click listener for save details button
            saveDetailsButton.setOnClickListener {
                val employeeId = textFieldEmployeeId.editText?.text.toString()
                val employeeName = textFieldEmployeeName.editText?.text.toString()
                val employeeDesignation = textFieldEmployeeDesignation.editText?.text.toString()

                if (employeeName.isEmpty() || employeeDesignation.isEmpty() || employeeId.isNullOrEmpty()) {
                    contextVar.toastyError("Employee Details Can't be Null")
                    return@setOnClickListener
                }
                val employeeIdInt = employeeId.toInt()
                if (checkIfEmployeeIdAlreadyExists(employeeIdInt)) {
                    contextVar.toastyError("Employee Id already exists")
                    return@setOnClickListener
                }
                saveDetailsButton.isClickable = false
                saveDetailsButton.isGone = true
                progressBarFragment.isVisible = true
                employeeObject =
                    EmployeeEntity(employeeIdInt, employeeName, employeeDesignation, "")
                // condition to check whether an image has been selected from the gallery
                if (dataOfImageSelected == null) {
                    contextVar.toastyError("Employee Image not selected")
                } else {
                    contextVar.toastyInfo("Adding employee please wait")
                    uploadImageToFirebase()
                }
            }
        }
    }

    /**
     * function to check whether id of employee already exists.
     * logic used - id's are added in hashset and then id is checked in hashet
     */
    private fun checkIfEmployeeIdAlreadyExists(employeeId: Int): Boolean {
        if (setOfEmployeeIds.contains(employeeId)) return true
        return false
    }

    /**
     * function to check whether id of employee already exists
     */
    private fun initializeViewModel() {
        contextVar = requireContext()
        viewModelProviderFactoryViewModelFactory =
            EmployeeAddFragmentViewModelFactory(
                EmployeeAddRepository()
            )
        viewModel = ViewModelProvider(this, viewModelProviderFactoryViewModelFactory).get(
            EmployeeAddFragmentViewModel::class.java
        )
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
                    viewModel.addAnEmployee(employeeObject)
                } else {
                    contextVar.toastyError(task.exception.toString())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                val returnUri = data?.data
                returnUri?.let {
                    dataOfImageSelected = returnUri
                    binding.textDisplayAddImage.isGone = true
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
}
