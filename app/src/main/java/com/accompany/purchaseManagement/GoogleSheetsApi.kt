package com.accompany.purchaseManagement

import com.accompany.purchaseManagement.data.SheetResponse
import retrofit2.Call
import retrofit2.http.*

interface GoogleSheetsApi {

    @POST("https://script.google.com/macros/s/AKfycbw9wp9dk_pdcwJHK8Im1n9db--dNu8lqSO9IQzZa1edlIJXOGyMa4HWs3pCBABRM3NVLA/exec")
    @FormUrlEncoded
    fun addPurchaseRequest(
        @Field("action") action: String = "addRequest",
        @Field("접수시간") requestTime: String,
        @Field("신청자명") applicantName: String,
        @Field("소속") department: String,
        @Field("장비/품목명") equipmentName: String,
        @Field("수량") quantity: String,
        @Field("장소") location: String,
        @Field("용도") purpose: String,
        @Field("기타사항") note: String,
        @Field("상태") status: String,
        @Field("사진첨부") photoUrls: String,
        @Field("처리완료일자") completedDate: String = ""
    ): Call<SheetResponse>
}


