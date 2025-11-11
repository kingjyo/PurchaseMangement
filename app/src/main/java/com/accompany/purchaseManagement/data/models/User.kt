package com.accompany.purchaseManagement.data.models

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 통합된 사용자 정보 데이터 클래스
 * Firebase Firestore, Google Sheets, 로컬 DB 모두에서 사용 가능
 */
data class User(
    @PropertyName("id")
    @SerializedName("id")
    val id: String = "",
    
    @PropertyName("name")
    @SerializedName("이름")
    val name: String = "",
    
    @PropertyName("email")
    @SerializedName("이메일")
    val email: String = "",
    
    @PropertyName("department")
    @SerializedName("소속")
    val department: String = "",
    
    @PropertyName("role")
    @SerializedName("역할")
    val role: String = ROLE_USER,
    
    @PropertyName("locationId")
    @SerializedName("지점ID")
    val locationId: String? = null,
    
    @PropertyName("locationName")
    @SerializedName("지점명")
    val locationName: String? = null,
    
    @PropertyName("profileImageUrl")
    @SerializedName("프로필이미지URL")
    val profileImageUrl: String? = null,
    
    @PropertyName("phoneNumber")
    @SerializedName("전화번호")
    val phoneNumber: String? = null,
    
    @PropertyName("fcmToken")
    @SerializedName("FCM토큰")
    val fcmToken: String? = null,
    
    @PropertyName("isActive")
    @SerializedName("활성상태")
    val isActive: Boolean = true,
    
    @PropertyName("createdAt")
    @SerializedName("생성일시")
    val createdAt: Long = System.currentTimeMillis(),
    
    @PropertyName("updatedAt")
    @SerializedName("수정일시")
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_MANAGER = "manager"
        const val ROLE_USER = "user"
    }
    
    fun isAdmin(): Boolean = role == ROLE_ADMIN
    fun isManager(): Boolean = role == ROLE_MANAGER || isAdmin()
    
    fun getDisplayRole(): String {
        return when (role) {
            ROLE_ADMIN -> "관리자"
            ROLE_MANAGER -> "매니저"
            ROLE_USER -> "사용자"
            else -> "사용자"
        }
    }
    
    /**
     * 지점 배정 여부 확인
     */
    fun hasLocation(): Boolean = !locationId.isNullOrEmpty()
    
    /**
     * 지점 정보 표시
     */
    fun getLocationDisplay(): String {
        return if (hasLocation()) {
            locationName ?: "지점 정보 없음"
        } else {
            "지점 미배정"
        }
    }
    
    /**
     * Firebase에서 사용할 수 있는 Map 형태로 변환
     */
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "department" to department,
            "role" to role,
            "locationId" to locationId,
            "locationName" to locationName,
            "profileImageUrl" to profileImageUrl,
            "phoneNumber" to phoneNumber,
            "fcmToken" to fcmToken,
            "isActive" to isActive,
            "createdAt" to createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
    }
}