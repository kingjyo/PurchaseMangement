package com.accompany.purchaseManagement

/**
 * Simple user information holder shared across the app.
 */
data class UserInfo(
    val email: String,
    val name: String,
    val department: String,
    val isAdmin: Boolean
)