package com.accompany.purchaseManagement

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// 개선된 구매신청 데이터 클래스
data class PurchaseRequestV2(
    val id: Long = 0,
    val requestId: String = "",  // Firebase 문서 ID

    // 신청자 정보 (자동 입력)
    @SerializedName("신청자명") val applicantName: String,
    @SerializedName("소속") val applicantDepartment: String,
    @SerializedName("이메일") val applicantEmail: String,

    // 구매 정보
    @SerializedName("장비명") val equipmentName: String,
    @SerializedName("수량") val quantity: String,
    @SerializedName("장소") val location: String = "",  // 선택사항
    @SerializedName("용도") val purpose: String,
    @SerializedName("기타사항") val note: String = "",

    // 사진 정보
    @SerializedName("사진URL") val photoUrls: List<String> = emptyList(),

    // 신청 정보
    @SerializedName("신청일시") val requestDate: String,
    @SerializedName("상태") val status: String = PurchaseStatus.PENDING.displayName,

    // 수정 정보
    @SerializedName("수정일시") val modifiedDate: String? = null,
    @SerializedName("수정횟수") val modifyCount: Int = 0,

    // 처리 정보
    @SerializedName("처리자") val processor: String? = null,
    @SerializedName("처리일시") val processedDate: String? = null,
    @SerializedName("처리메모") val processNote: String? = null
) : Serializable {

    // 수정 가능 여부 확인
    fun isModifiable(): Boolean {
        val status = PurchaseStatus.fromString(this.status)
        return status in listOf(
            PurchaseStatus.PENDING,
            PurchaseStatus.CONFIRMED
        )
    }

    // 상태 변경 가능 여부 확인
    fun canChangeStatus(): Boolean {
        val status = PurchaseStatus.fromString(this.status)
        return status != PurchaseStatus.COMPLETED
    }

    // Firebase 저장용 Map 변환
    fun toFirebaseMap(): Map<String, Any> {
        return mapOf(
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
            "modifiedDate" to (modifiedDate ?: requestDate),
            "modifyCount" to modifyCount,
            "processor" to (processor ?: ""),
            "processedDate" to (processedDate ?: ""),
            "processNote" to (processNote ?: "")
        )
    }

    companion object {
        // Firebase 문서에서 객체 생성
        fun fromFirebaseDocument(
            documentId: String,
            data: Map<String, Any>
        ): PurchaseRequestV2 {
            return PurchaseRequestV2(
                requestId = documentId,
                applicantName = data["applicantName"] as? String ?: "",
                applicantDepartment = data["applicantDepartment"] as? String ?: "",
                applicantEmail = data["applicantEmail"] as? String ?: "",
                equipmentName = data["equipmentName"] as? String ?: "",
                quantity = data["quantity"] as? String ?: "1",
                location = data["location"] as? String ?: "",
                purpose = data["purpose"] as? String ?: "",
                note = data["note"] as? String ?: "",
                photoUrls = (data["photoUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                requestDate = data["requestDate"] as? String ?: "",
                status = data["status"] as? String ?: PurchaseStatus.PENDING.displayName,
                modifiedDate = data["modifiedDate"] as? String,
                modifyCount = (data["modifyCount"] as? Long)?.toInt() ?: 0,
                processor = data["processor"] as? String,
                processedDate = data["processedDate"] as? String,
                processNote = data["processNote"] as? String
            )
        }
    }
}

// 수정 요청 데이터
data class ModifyRequest(
    val requestId: String,
    val equipmentName: String? = null,
    val quantity: String? = null,
    val location: String? = null,
    val purpose: String? = null,
    val note: String? = null,
    val photoUrls: List<String>? = null
) {
    // 변경된 필드 목록 반환
    fun getModifiedFields(): List<String> {
        val fields = mutableListOf<String>()
        if (equipmentName != null) fields.add("장비명")
        if (quantity != null) fields.add("수량")
        if (location != null) fields.add("장소")
        if (purpose != null) fields.add("용도")
        if (note != null) fields.add("기타사항")
        if (photoUrls != null) fields.add("사진")
        return fields
    }
}