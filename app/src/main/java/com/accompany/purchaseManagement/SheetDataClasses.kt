package com.accompany.purchaseManagement.data

import com.accompany.purchaseManagement.PurchaseRequestV2
import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Google Sheets 응답 데이터
data class SheetResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("rowNumber") val rowNumber: Int? = null,
    @SerializedName("data") val data: Any? = null
)

// Google Sheets 요청/응답용 데이터 클래스
data class SheetRequest(
    @SerializedName("rowNumber") val rowNumber: Int,
    @SerializedName("접수시간") val requestTime: String,
    @SerializedName("신청자명") val applicantName: String,
    @SerializedName("소속") val department: String,
    @SerializedName("장비품목명") val equipmentName: String,
    @SerializedName("수량") val quantity: String,
    @SerializedName("장소") val location: String,
    @SerializedName("용도") val purpose: String,
    @SerializedName("기타사항") val note: String,
    @SerializedName("상태") val status: String,
    @SerializedName("사진첨부") val photoUrls: String,
    @SerializedName("처리완료일자") val completedDate: String
) : Serializable {

    // PurchaseRequestV2로 변환
    fun toPurchaseRequestV2(): PurchaseRequestV2 {
        return PurchaseRequestV2(
            requestId = rowNumber.toString(),
            applicantName = applicantName,
            applicantDepartment = department,
            applicantEmail = "", // 이메일은 별도로 관리
            equipmentName = equipmentName,
            quantity = quantity,
            location = location,
            purpose = purpose,
            note = note,
            photoUrls = if (photoUrls.isNotEmpty()) photoUrls.split(",") else emptyList(),
            requestDate = requestTime,
            status = status,
            processedDate = completedDate.ifEmpty { null }
        )
    }
}

// 구매신청 생성 요청 데이터
data class CreatePurchaseRequest(
    val applicantName: String,
    val department: String,
    val equipmentName: String,
    val quantity: String,
    val location: String,
    val purpose: String,
    val note: String,
    val photoUrls: List<String> = emptyList()
) {
    // Validation
    fun isValid(): Boolean {
        return applicantName.isNotBlank() &&
                department.isNotBlank() &&
                equipmentName.isNotBlank() &&
                quantity.isNotBlank() &&
                purpose.isNotBlank()
    }

    // 사진 URL 문자열로 변환 (콤마 구분)
    fun getPhotoUrlsString(): String {
        return photoUrls.joinToString(",")
    }
}