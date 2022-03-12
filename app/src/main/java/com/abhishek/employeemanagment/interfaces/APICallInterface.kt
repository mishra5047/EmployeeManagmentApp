package com.abhishek.employeemanagment.interfaces

import com.abhishek.employeemanagment.data.remote.EmployeeClassAPIResponse
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Singleton

@Singleton
interface APICallInterface {
    @GET("/allEmployees")
    suspend fun getAllEmployeesAPI(): Response<List<EmployeeClassAPIResponse>>

    @DELETE("/deleteAllEmployees")
    suspend fun deleteAllEmployeesAPI(): Response<String>

    @DELETE("/deleteAnEmployee")
    suspend fun deleteAnEmployeesAPI(@Query("id") idOfEmployee: Int): Response<String>

    @POST("/addEmployee")
    suspend fun addAnEmployeesAPI(
        @Query("id") idOfEmployee: Int,
        @Query("name") nameOfEmployee: String,
        @Query("designation") designationOfEmployee: String,
        @Query("picURL") imageUrl: String
    ): Response<String>

    @DELETE("/deleteMultipleEmployees")
    suspend fun deleteMultipleEmployees(@Query("listOfEmployees") listOfEmployeesId: ArrayList<Int>): Response<String>

    @PUT("/updateEmployee")
    suspend fun updateAnEmployeesAPI(
        @Query("id") idOfEmployee: Int,
        @Query("name") nameOfEmployee: String,
        @Query("designation") designationOfEmployee: String,
        @Query("picURL") imageUrl: String
    ): Response<String>

    @GET("/getLastTimeStamp")
    suspend fun getLastTimeStamp(): Response<String>
}
