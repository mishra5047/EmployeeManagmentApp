package com.abhishek.employeemanagment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.data.remote.EmployeeClassAPIResponse
import com.abhishek.employeemanagment.data.repository.HomeFragmentRepository
import com.abhishek.employeemanagment.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class HomeFragmentViewModel(private val repository: HomeFragmentRepository) : ViewModel() {

    val employeesGetterOffline: LiveData<Resource<List<EmployeeEntity>>> get() = _employeesListOffline
    private val _employeesListOffline = MutableLiveData<Resource<List<EmployeeEntity>>>()

    val employeesGetterOnline: LiveData<Resource<List<EmployeeClassAPIResponse>>> get() = _employeesListOnline
    private val _employeesListOnline = MutableLiveData<Resource<List<EmployeeClassAPIResponse>>>()

    val deleteEmployeesGetter: LiveData<Resource<String>> get() = _deleteEmployee
    private val _deleteEmployee = MutableLiveData<Resource<String>>()

    fun getAllEmployeesOffline() = viewModelScope.launch(Dispatchers.IO) {
        try {
            _employeesListOffline.postValue(Resource.Loading())
            val response = repository.getAllEmployeesOffline()
            _employeesListOffline.postValue(Resource.Success(response))
        } catch (e: Exception) {
            _employeesListOffline.postValue(Resource.Error(e.toString()))
        }
    }

    fun updateRoomDBTable(employeesList: List<EmployeeClassAPIResponse>) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllEmployeesOffline()
            for (employeeObject in employeesList) {
                repository.insertEmployeeOffline(
                    EmployeeEntity(
                        employeeObject.Id,
                        employeeObject.Name,
                        employeeObject.Designation,
                        employeeObject.ProfilePicURL
                    )
                )
            }
            getAllEmployeesOffline()
        }

    fun getAllEmployeesOnline() = viewModelScope.launch(Dispatchers.IO) {
        try {
            _employeesListOnline.postValue(Resource.Loading())
            val response = repository.getAllEmployeesOnline()
            _employeesListOnline.postValue(handleAPIReturnResponse(response))
        } catch (e: Exception) {
            _employeesListOnline.postValue(Resource.Error(e.toString()))
        }
    }

    private fun handleAPIReturnResponse(response: Response<List<EmployeeClassAPIResponse>>): Resource<List<EmployeeClassAPIResponse>> {
        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.errorBody().toString())
    }

    fun deleteAllEmployeesOffline() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllEmployeesOffline()
    }

    fun deleteOneEmployee(employeeId : Int) = viewModelScope.launch(Dispatchers.IO){
        try{
            _deleteEmployee.postValue(Resource.Loading())
            val response = repository.deleteAnEmployeeOnline(employeeId)
            _deleteEmployee.postValue(handleDeleteEmployeeResponse(response))
        }catch (e : Exception){
            _deleteEmployee.postValue(Resource.Error(e.toString()))
        }
    }

    fun deleteMultipleEmployees(listOfEmployeeIds: HashSet<Int>) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _deleteEmployee.postValue(Resource.Loading())
                val listOfId = ArrayList<Int>()
                for (id in listOfEmployeeIds) {
                    listOfId.add(id)
                }
                val response = repository.deleteMultipleEmployees(listOfId)
                _deleteEmployee.postValue(handleDeleteEmployeeResponse(response))
            } catch (e: Exception) {
                _deleteEmployee.postValue(Resource.Error(e.toString()))
            }
        }

    private fun handleDeleteEmployeeResponse(response: Response<String>): Resource<String> {
        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.errorBody().toString())
    }
}
