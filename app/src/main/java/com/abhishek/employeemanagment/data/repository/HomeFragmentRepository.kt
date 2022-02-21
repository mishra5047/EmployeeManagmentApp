package com.abhishek.employeemanagment.data.repository

import com.abhishek.employeemanagment.data.local.EmployeeDatabase
import com.abhishek.employeemanagment.data.model.EmployeeDao
import com.abhishek.employeemanagment.data.model.EmployeeEntity
import com.abhishek.employeemanagment.di.APICallInterface
import com.abhishek.employeemanagment.di.RetrofitClient
import retrofit2.Response

class HomeFragmentRepository(database: EmployeeDatabase) {

    private var employeeDao: EmployeeDao = database.employeeDao()
    private val retrofitInstance = RetrofitClient.getInstance().create(APICallInterface::class.java)

    fun deleteAllEmployeesOffline() = employeeDao.deleteAllEmployees()

    fun insertEmployeeOffline(employeeEntity: EmployeeEntity) =
        employeeDao.insertEmployee(employeeEntity)

    suspend fun deleteAllEmployees() = retrofitInstance.deleteAllEmployeesAPI()

    suspend fun deleteAnEmployee(employeeId: Int): Response<String> =
        retrofitInstance.deleteAnEmployeesAPI(employeeId)

    suspend fun deleteMultipleEmployees(listOfEmployeeId: ArrayList<Int>): Response<String> =
        retrofitInstance.deleteMultipleEmployees(listOfEmployeeId)

    fun getAllEmployeesOffline() = employeeDao.getAllEmployees()

    suspend fun getAllEmployeesOnline() = retrofitInstance.getAllEmployeesAPI()

    suspend fun getLastUpdateTimeStamp() = retrofitInstance.getLastTimeStamp()
}
