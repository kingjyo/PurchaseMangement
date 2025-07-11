package com.accompany.purchaseManagement

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbwrVEWgXV8_hf686Q67jmOU0Dv9Z6iUUsZMYliRyTBavj31NGDYYhwPTIzG6GL3309YjA/exec/"

    val api: GoogleSheetsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleSheetsApi::class.java)
    }
}