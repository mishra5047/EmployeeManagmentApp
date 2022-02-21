package com.abhishek.employeemanagment.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abhishek.employeemanagment.data.repository.EmployeeDetailRepository
import com.abhishek.employeemanagment.viewmodel.EmployeeDetailFragmentViewModel

class EmployeeDetailFragmentViewModelFactory(private val repository: EmployeeDetailRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EmployeeDetailFragmentViewModel(repository) as T
    }
}
