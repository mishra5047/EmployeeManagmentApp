package com.abhishek.employeemanagment.interfaces

import com.abhishek.employeemanagment.util.Constants
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    var gson = GsonBuilder()
        .setLenient()
        .create()

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
