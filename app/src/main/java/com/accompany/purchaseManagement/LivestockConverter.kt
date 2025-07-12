package com.accompany.purchaseManagement

import com.accompany.purchaseManagement.Livestock
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// ============ 구글 스프레드시트용 데이터 클래스 ============
data class CattleFromSheet(
    @SerializedName("관리번호") val managementNumber: String,
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

object LivestockConverter {

    // 스프레드시트 데이터를 Firestore 모델로 변환
    fun fromSheetToFirestore(cattleData: CattleFromSheet): Livestock {
        return Livestock(
            managementNumber = cattleData.managementNumber,
            tagNumber = cattleData.tagNumber,
            eartagNumber = cattleData.eartagNumber,
            birthDate = cattleData.birthDate,
            monthAge = cattleData.monthAge,
            type = cattleData.type,
            gender = cattleData.gender,
            weight = cattleData.weight,
            barn = cattleData.barn,
            status = cattleData.status,
            isAvailable = cattleData.status != "출하" && cattleData.status != "폐사"
        )
    }

    // JSON 문자열을 CattleFromSheet 객체로 변환
    fun fromJsonToSheet(json: String): CattleFromSheet {
        return Gson().fromJson(json, CattleFromSheet::class.java)
    }

    // Firestore 모델을 스프레드시트 형식으로 변환
    fun fromFirestoreToSheet(livestock: Livestock): Map<String, Any?> {
        return mapOf(
            "관리번호" to livestock.managementNumber,
            "개체번호" to livestock.tagNumber,
            "이표번호" to livestock.eartagNumber,
            "생년월일" to livestock.birthDate,
            "월령" to livestock.monthAge,
            "개체구분" to livestock.type,
            "성별" to livestock.gender,
            "체중" to livestock.weight,
            "축사" to livestock.barn,
            "상태" to livestock.status
        )
    }
}