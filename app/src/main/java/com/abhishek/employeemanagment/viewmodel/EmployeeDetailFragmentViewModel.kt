package com.abhishek.employeemanagment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.data.repository.EmployeeDetailRepository
import com.abhishek.employeemanagment.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class EmployeeDetailFragmentViewModel @Inject constructor(private val repository: EmployeeDetailRepository) :
    ViewModel() {

    val updateAnEmployeeGetter: LiveData<Resource<String>> get() = _updateAnEmployeeList
    private val _updateAnEmployeeList = MutableLiveData<Resource<String>>()

    val deleteAnEmployeeGetter: LiveData<Resource<String>> get() = _deleteAnEmployeeList
    private val _deleteAnEmployeeList = MutableLiveData<Resource<String>>()

    fun updateAnEmployeeOnline(employeeEntity: EmployeeEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            _updateAnEmployeeList.postValue(Resource.Loading())
            val response = repository.updateAnEmployeeOnline(employeeEntity)
            _updateAnEmployeeList.postValue(handleUpdateEmployeeResponse(response))
        }

    private fun handleUpdateEmployeeResponse(response: Response<String>): Resource<String> {
        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.errorBody().toString())
    }

    fun deleteAnEmployeeOnline(employeeId: Int) = viewModelScope.launch(Dispatchers.IO) {
        _deleteAnEmployeeList.postValue(Resource.Loading())
        val response = repository.deleteAnEmployeeOnline(employeeId = employeeId)
        _deleteAnEmployeeList.postValue(handleDeleteEmployeeResponse(response))
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
