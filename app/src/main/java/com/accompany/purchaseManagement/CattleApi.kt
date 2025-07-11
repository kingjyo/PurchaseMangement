package com.accompany.purchaseManagement

import com.accompany.purchaseManagement.Cattle
import retrofit2.http.GET

interface CattleApi {
    @GET("https://script.google.com/macros/s/AKfycbzUPcgAfT0WUb47HDvdfYY-wQrtsxkDseovQRxgFOaoWm4KzsxR8bXDU2q5M7JtQAOHJA/exec")
    suspend fun getCattleList(): List<Cattle>
}