package com.abhishek.employeemanagment.data.repository

import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.di.APICallInterface
import com.abhishek.employeemanagment.di.RetrofitClient

class EmployeeAddRepository {

    private val retrofitInstance = RetrofitClient.getInstance().create(APICallInterface::class.java)
    suspend fun addAnEmployee(employeeEntity: EmployeeEntity) = retrofitInstance.addAnEmployeesAPI(
        employeeEntity.id,
        employeeEntity.name,
        employeeEntity.designation,
        employeeEntity.profilePicUrl
    )
}
