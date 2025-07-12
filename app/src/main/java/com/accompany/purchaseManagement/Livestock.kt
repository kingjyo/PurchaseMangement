package com.accompany.purchaseManagement

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

// ============ 기존 Cattle 모델 기반으로 전면 수정 ============
data class Livestock(
    @DocumentId
    val id: String = "",  // <-- 변경: Firestore 문서 ID

    @PropertyName("managementNumber")
    val managementNumber: String = "",  // <-- 변경: 관리번호 (기존 id)

    @PropertyName("tagNumber")
    val tagNumber: String = "",  // <-- 유지: 개체번호

    @PropertyName("eartagNumber")
    val eartagNumber: String? = null,  // <-- 유지: 이표번호

    @PropertyName("birthDate")
    val birthDate: String = "",  // <-- 유지: 생년월일 (YYYY-MM-DD)

    @PropertyName("monthAge")
    val monthAge: Int = 0,  // <-- 유지: 월령

    @PropertyName("type")
    val type: String = "",  // <-- 변경: 개체구분 (송아지, 비육우, 번식우 등)

    @PropertyName("gender")
    val gender: String = "",  // <-- 유지: 성별

    @PropertyName("weight")
    val weight: Float? = null,  // <-- 유지: 체중 (kg)

    @PropertyName("barn")
    val barn: String = "",  // <-- 유지: 축사

    @PropertyName("status")
    val status: String? = null,  // <-- 유지: 상태 (사육중, 출하, 폐사 등)

    // ============ Firebase용 추가 필드 시작 ============

    @PropertyName("healthStatus")
    val healthStatus: String = "healthy",  // <-- 추가: 건강상태

    @PropertyName("vaccinations")
    val vaccinations: List<Map<String, Any>> = emptyList(),  // <-- 추가: 백신접종 기록

    @PropertyName("imageUrls")
    val imageUrls: List<String> = emptyList(),  // <-- 추가: 이미지 URL 목록

    @PropertyName("notes")
    val notes: String = "",  // <-- 추가: 비고

    @PropertyName("isAvailable")
    val isAvailable: Boolean = true,  // <-- 추가: 구매 가능 여부

    @ServerTimestamp
    @PropertyName("createdAt")
    val createdAt: Timestamp? = null,  // <-- 추가: 등록일시

    @PropertyName("updatedAt")
    val updatedAt: Timestamp? = null,  // <-- 추가: 수정일시

    // ============ Firebase용 추가 필드 끝 ============
) {
    // 성별 텍스트 반환
    fun getGenderText(): String {
        return when (gender.lowercase()) {
            "m", "male", "수" -> "수컷"
            "f", "female", "암" -> "암컷"
            "거세" -> "거세"
            else -> gender
        }
    }

    // 상태 텍스트 반환
    fun getStatusText(): String {
        return status ?: "사육중"
    }

    // 나이 계산 (년.월 형식)
    fun getAgeText(): String {
        val years = monthAge / 12
        val months = monthAge % 12
        return if (years > 0) {
            "${years}년 ${months}개월"
        } else {
            "${months}개월"
        }
    }

    // 개체 식별 정보 (개체번호 + 이표번호)
    fun getIdentificationText(): String {
        return if (!eartagNumber.isNullOrEmpty()) {
            "$tagNumber ($eartagNumber)"
        } else {
            tagNumber
        }
    }
}