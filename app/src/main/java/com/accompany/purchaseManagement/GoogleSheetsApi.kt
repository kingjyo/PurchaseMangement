package com.accompany.purchaseManagement

import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleSheetsApi {
    @GET("https://script.google.com/macros/s/AKfycbwrVEWgXV8_hf686Q67jmOU0Dv9Z6iUUsZMYliRyTBavj31NGDYYhwPTIzG6GL3309YjA/exec")  // Apps Script 도메인 URL 넣기
    suspend fun getPurchaseRequests(): List<purchaseManagement>
}

interface CattleStatusApi {
    @GET("https://script.google.com/macros/s/AKfycbzUPcgAfT0WUb47HDvdfYY-wQrtsxkDseovQRxgFOaoWm4KzsxR8bXDU2q5M7JtQAOHJA/exec") // 예시: "exec" 뒤에 "/"
    suspend fun getCattleList(): List<Cattle>
}