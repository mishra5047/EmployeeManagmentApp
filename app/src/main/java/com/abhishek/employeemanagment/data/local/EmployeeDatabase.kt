package com.abhishek.employeemanagment.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abhishek.employeemanagment.data.model.EmployeeDao
import com.abhishek.employeemanagment.data.model.EmployeeEntity

@Database(entities = [EmployeeEntity::class], version = 1)
abstract class EmployeeDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao

    companion object {
        @Volatile
        private var instance: EmployeeDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) = Room
            .databaseBuilder(
                context.applicationContext,
                EmployeeDatabase::class.java,
                "employee.db"
            )
            .build()
    }
}
