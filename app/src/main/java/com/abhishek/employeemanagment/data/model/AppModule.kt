package com.abhishek.employeemanagment.data.model

import android.content.Context
import androidx.room.Room
import com.abhishek.employeemanagment.data.local.EmployeeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun createDatabase(@ApplicationContext context: Context) = Room
        .databaseBuilder(
            context,
            EmployeeDatabase::class.java,
            "employee.db"
        )
        .build()

    @Singleton
    @Provides
    fun provideDao(db: EmployeeDatabase) = db.employeeDao()
}
