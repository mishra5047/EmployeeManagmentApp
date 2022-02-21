package com.abhishek.employeemanagment.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.abhishek.employeemanagment.data.repository.HomeFragmentRepository
import com.abhishek.employeemanagment.viewmodel.HomeFragmentViewModel

class HomeFragmentViewModelFactory(private val repository: HomeFragmentRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeFragmentViewModel(repository) as T
    }
}
