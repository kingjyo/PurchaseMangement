package com.accompany.purchaseManagement.utils

import android.util.Patterns

object ValidationUtils {

    fun isValidEquipmentName(name: String): Boolean {
        return name.trim().length >= 2
    }

    fun isValidQuantity(quantity: String): Boolean {
        val qty = quantity.toIntOrNull()
        return qty != null && qty > 0
    }

    fun isValidPurpose(purpose: String): Boolean {
        return purpose.trim().length >= 3
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}