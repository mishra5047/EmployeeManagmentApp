package com.abhishek.employeemanagment.data.repository

import com.abhishek.employeemanagment.data.local.EmployeeDatabase
import com.abhishek.employeemanagment.data.model.EmployeeDao
import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.interfaces.APICallInterface
import com.abhishek.employeemanagment.interfaces.RetrofitClient
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeFragmentRepository @Inject constructor(private val employeeDao: EmployeeDao) {
    private val retrofitInstance = RetrofitClient.getInstance().create(APICallInterface::class.java)

    fun deleteAllEmployeesOffline() = employeeDao.deleteAllEmployees()

    fun insertEmployeeOffline(employeeEntity: EmployeeEntity) =
        employeeDao.insertEmployee(employeeEntity)

    suspend fun deleteMultipleEmployees(listOfEmployeeId: ArrayList<Int>): Response<String> =
        retrofitInstance.deleteMultipleEmployees(listOfEmployeeId)

    fun getAllEmployeesOffline() = employeeDao.getAllEmployees()

    suspend fun getAllEmployeesOnline() = retrofitInstance.getAllEmployeesAPI()

    suspend fun deleteAnEmployeeOnline(employeeId: Int): Response<String> =
        retrofitInstance.deleteAnEmployeesAPI(employeeId)
}
