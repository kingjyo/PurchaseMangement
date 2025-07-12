package com.accompany.PurchaseManagement

import com.google.gson.annotations.SerializedName

data class Cattle(
    @SerializedName("관리번호") val id: String,
    @SerializedName("개체번호") val tagNumber: String,
    @SerializedName("이표번호") val eartagNumber: String? = null,
    @SerializedName("생년월일") val birthDate: String,
    @SerializedName("월령") val monthAge: Int,
    @SerializedName("개체구분") val type: String,
    @SerializedName("성별") val gender: String,
    @SerializedName("체중") val weight: Float? = null,
    @SerializedName("축사") val barn: String,
    @SerializedName("상태") val status: String? = null
)

