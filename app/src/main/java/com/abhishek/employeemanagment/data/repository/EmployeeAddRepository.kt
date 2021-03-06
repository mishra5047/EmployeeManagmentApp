package com.abhishek.employeemanagment.data.repository

import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.interfaces.APICallInterface
import com.abhishek.employeemanagment.interfaces.RetrofitClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeAddRepository @Inject constructor() {
    private val retrofitInstance = RetrofitClient.getInstance().create(APICallInterface::class.java)
    suspend fun addAnEmployee(employeeEntity: EmployeeEntity) = retrofitInstance.addAnEmployeesAPI(
        employeeEntity.id,
        employeeEntity.name,
        employeeEntity.designation,
        employeeEntity.profilePicUrl
    )
}
