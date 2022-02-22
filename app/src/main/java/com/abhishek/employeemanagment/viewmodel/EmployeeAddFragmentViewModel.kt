package com.abhishek.employeemanagment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.data.repository.EmployeeAddRepository
import com.abhishek.employeemanagment.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class EmployeeAddFragmentViewModel(private val repository: EmployeeAddRepository) : ViewModel() {

    val addAnEmployeeGetter: LiveData<Resource<String>> get() = _addAnEmployeeList
    private val _addAnEmployeeList = MutableLiveData<Resource<String>>()

    fun addAnEmployee(employeeEntity: EmployeeEntity) = viewModelScope.launch(Dispatchers.IO) {
        _addAnEmployeeList.postValue(Resource.Loading())
        val response = repository.addAnEmployee(employeeEntity)
        _addAnEmployeeList.postValue(handleAddResponse(response))
    }

    private fun handleAddResponse(response: Response<String>): Resource<String>? {
        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it.toString())
            }
        }
        return Resource.Error(response.errorBody().toString())
    }
}
