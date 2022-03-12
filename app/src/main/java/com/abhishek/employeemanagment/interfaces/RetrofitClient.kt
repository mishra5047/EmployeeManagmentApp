package com.abhishek.employeemanagment.interfaces

import com.abhishek.employeemanagment.util.Constants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// provides instance of retrofit for the application
@Module
@InstallIn(SingletonComponent::class)
object RetrofitClient {

    var gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    @Singleton
    @Provides
    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
