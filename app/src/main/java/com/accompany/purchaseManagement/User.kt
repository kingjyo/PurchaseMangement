package com.accompany.purchaseManagement

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class User(
    @DocumentId
    val id: String = "",

    @PropertyName("email")
    val email: String = "",

    @PropertyName("name")
    val name: String = "",

    @PropertyName("role")
    val role: String = "user", // admin, manager, user

    @PropertyName("department")
    val department: String = "",

    @PropertyName("phone")
    val phone: String = "",

    @PropertyName("profileImageUrl")
    val profileImageUrl: String? = null,

    @PropertyName("isActive")
    val isActive: Boolean = true,

    @ServerTimestamp
    @PropertyName("createdAt")
    val createdAt: Timestamp? = null,

    @PropertyName("lastLoginAt")
    val lastLoginAt: Timestamp? = null,

    @PropertyName("fcmToken")
    val fcmToken: String? = null
) {
    // 관리자 권한 체크
    fun isAdmin(): Boolean = role == "admin"

    // 매니저 이상 권한 체크
    fun isManagerOrAbove(): Boolean = role == "admin" || role == "manager"
}