package com.accompany.purchaseManagement

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CattleStatusRetrofit {
    private const val BASE_URL = "https://script.google.com/macros/s/여기에_ID/exec/"

    val api: CattleStatusApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CattleStatusApi::class.java)
    }
}