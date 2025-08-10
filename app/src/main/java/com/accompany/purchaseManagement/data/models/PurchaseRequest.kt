package com.accompany.purchaseManagement.data.models

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 통합된 구매신청 데이터 클래스
 * Firebase Firestore, Google Sheets, 로컬 DB 모두에서 사용 가능
 */
data class PurchaseRequest(
    // 기본 식별자
    @PropertyName("id")
    @SerializedName("ID")
    val id: String = "",
    
    @PropertyName("localId")
    val localId: Long = 0L, // 로컬 DB용 ID
    
    // 신청자 정보 (자동 입력)
    @PropertyName("applicantId")
    @SerializedName("신청자ID")
    val applicantId: String = "",
    
    @PropertyName("applicantName")
    @SerializedName("신청자명")
    val applicantName: String = "",
    
    @PropertyName("applicantDepartment")
    @SerializedName("소속")
    val applicantDepartment: String = "",
    
    @PropertyName("applicantEmail")
    @SerializedName("이메일")
    val applicantEmail: String = "",
    
    // 구매 정보
    @PropertyName("equipmentName")
    @SerializedName("장비/품목명")
    val equipmentName: String = "",
    
    @PropertyName("quantity")
    @SerializedName("수량")
    val quantity: String = "",
    
    @PropertyName("location")
    @SerializedName("장소")
    val location: String = "",
    
    @PropertyName("purpose")
    @SerializedName("용도")
    val purpose: String = "",
    
    @PropertyName("note")
    @SerializedName("기타사항")
    val note: String = "",
    
    // 사진 정보
    @PropertyName("photoUrls")
    @SerializedName("사진URL")
    val photoUrls: List<String> = emptyList(),
    
    // 신청 정보
    @PropertyName("requestDate")
    @SerializedName("신청일시")
    val requestDate: String = "",
    
    @PropertyName("status")
    @SerializedName("상태")
    val status: String = STATUS_PENDING,
    
    // 수정 정보
    @PropertyName("modifiedDate")
    @SerializedName("수정일시")
    val modifiedDate: String? = null,
    
    @PropertyName("modifyCount")
    @SerializedName("수정횟수")
    val modifyCount: Int = 0,
    
    // 처리 정보
    @PropertyName("processor")
    @SerializedName("처리자")
    val processor: String? = null,
    
    @PropertyName("processedDate")
    @SerializedName("처리일시")
    val processedDate: String? = null,
    
    @PropertyName("processNote")
    @SerializedName("처리메모")
    val processNote: String? = null,
    
    // 메타 정보
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
    
) : Serializable {
    
    companion object {
        const val STATUS_PENDING = "대기중"
        const val STATUS_APPROVED = "승인됨"
        const val STATUS_REJECTED = "거부됨"
        const val STATUS_IN_PROGRESS = "진행중"
        const val STATUS_COMPLETED = "완료됨"
        const val STATUS_CANCELLED = "취소됨"
    }
    
    /**
     * 수정 가능 여부 확인
     */
    fun canModify(): Boolean {
        return status == STATUS_PENDING || status == STATUS_REJECTED
    }
    
    /**
     * 완료 상태 확인
     */
    fun isCompleted(): Boolean {
        return status == STATUS_COMPLETED || status == STATUS_CANCELLED
    }
    
    /**
     * 상태 표시를 위한 색상 리소스 ID
     */
    fun getStatusColor(): Int {
        return when (status) {
            STATUS_PENDING -> android.R.color.holo_orange_dark
            STATUS_APPROVED -> android.R.color.holo_green_dark
            STATUS_REJECTED -> android.R.color.holo_red_dark
            STATUS_IN_PROGRESS -> android.R.color.holo_blue_dark
            STATUS_COMPLETED -> android.R.color.holo_green_light
            STATUS_CANCELLED -> android.R.color.darker_gray
            else -> android.R.color.black
        }
    }
    
    /**
     * Firebase에서 사용할 수 있는 Map 형태로 변환
     */
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "applicantId" to applicantId,
            "applicantName" to applicantName,
            "applicantDepartment" to applicantDepartment,
            "applicantEmail" to applicantEmail,
            "equipmentName" to equipmentName,
            "quantity" to quantity,
            "location" to location,
            "purpose" to purpose,
            "note" to note,
            "photoUrls" to photoUrls,
            "requestDate" to requestDate,
            "status" to status,
            "modifiedDate" to modifiedDate,
            "modifyCount" to modifyCount,
            "processor" to processor,
            "processedDate" to processedDate,
            "processNote" to processNote,
            "createdAt" to createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
    }
    
    /**
     * 유효성 검사
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (equipmentName.isBlank()) errors.add("장비/품목명을 입력해주세요")
        if (quantity.isBlank()) errors.add("수량을 입력해주세요")
        if (purpose.isBlank()) errors.add("용도를 입력해주세요")
        if (applicantName.isBlank()) errors.add("신청자명이 없습니다")
        if (applicantDepartment.isBlank()) errors.add("소속이 없습니다")
        if (applicantEmail.isBlank()) errors.add("이메일이 없습니다")
        
        return errors
    }
}