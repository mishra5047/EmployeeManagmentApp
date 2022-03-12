package com.abhishek.employeemanagment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abhishek.employeemanagment.data.model.EmployeeDao
import com.abhishek.employeemanagment.data.model.EmployeeEntity

@Database(entities = [EmployeeEntity::class], version = 1)
abstract class EmployeeDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
}
