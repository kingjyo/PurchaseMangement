package com.accompany.purchaseManagement.api

import com.accompany.purchaseManagement.data.SheetResponse
import retrofit2.Call
import retrofit2.http.*

interface GoogleSheetsApi {

    @POST("https://script.google.com/macros/s/AKfycbxqugzxUsgEz3rEjqKVtOkZb7vau1dS0O0Ec8H6Xc4HAorzOtaAbP_2o4ELYdRX32GTsQ/exec")
    @FormUrlEncoded
    fun addPurchaseRequest(
        @Field("action") action: String = "addRequest",
        @Field("접수시간") requestTime: String,
        @Field("신청자명") applicantName: String,
        @Field("소속") department: String,
        @Field("장비품목명") equipmentName: String,
        @Field("수량") quantity: String,
        @Field("장소") location: String,
        @Field("용도") purpose: String,
        @Field("기타사항") note: String,
        @Field("상태") status: String,
        @Field("사진첨부") photoUrls: String,
        @Field("처리완료일자") completedDate: String = ""
    ): Call<SheetResponse>
}

