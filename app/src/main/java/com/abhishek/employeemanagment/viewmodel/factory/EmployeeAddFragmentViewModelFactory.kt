package com.abhishek.employeemanagment.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abhishek.employeemanagment.data.repository.EmployeeAddRepository
import com.abhishek.employeemanagment.viewmodel.EmployeeAddFragmentViewModel

class EmployeeAddFragmentViewModelFactory(private val repository: EmployeeAddRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EmployeeAddFragmentViewModel(repository) as T
    }
}
