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
     * Firebase에서 사용할 수 있는 Map 형태로 변환
     */
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "department" to department,
            "role" to role,
            "profileImageUrl" to profileImageUrl,
            "phoneNumber" to phoneNumber,
            "fcmToken" to fcmToken,
            "isActive" to isActive,
            "createdAt" to createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
    }
}