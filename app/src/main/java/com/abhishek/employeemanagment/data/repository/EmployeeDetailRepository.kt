package com.abhishek.employeemanagment.data.repository

import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.di.APICallInterface
import com.abhishek.employeemanagment.di.RetrofitClient
import retrofit2.Response

class EmployeeDetailRepository {

    private val retrofitInstance = RetrofitClient.getInstance().create(APICallInterface::class.java)

    suspend fun updateAnEmployeeOnline(employeeEntity: EmployeeEntity) =
        retrofitInstance.updateAnEmployeesAPI(
            employeeEntity.id,
            employeeEntity.name,
            employeeEntity.designation,
            employeeEntity.profilePicUrl
        )

    suspend fun deleteAnEmployeeOnline(employeeId: Int): Response<String> =
        retrofitInstance.deleteAnEmployeesAPI(employeeId)
}
