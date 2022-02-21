package com.abhishek.employeemanagment.data.model

import androidx.room.*
import com.abhishek.employeemanagment.util.Constants

@Dao
interface EmployeeDao {

    companion object {
        const val roomDBName = Constants.ROOM_DB_NAME
    }

    @Query("SELECT * FROM $roomDBName")
    fun getAllEmployees(): List<EmployeeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEmployee(employeeEntity: EmployeeEntity)

    @Update
    fun updateEmployee(employeeEntity: EmployeeEntity)

    @Query("DELETE FROM $roomDBName")
    fun deleteAllEmployees()

    @Delete
    fun delete(employeeEntity: EmployeeEntity)

    @Query("DELETE FROM $roomDBName WHERE Id in (:idLists)")
    fun deleteMultipleEmployees(idLists: List<Int>)
}
