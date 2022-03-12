package com.abhishek.employeemanagment.data.repository

import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.interfaces.APICallInterface
import com.abhishek.employeemanagment.interfaces.RetrofitClient
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeDetailRepository @Inject constructor() {

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
