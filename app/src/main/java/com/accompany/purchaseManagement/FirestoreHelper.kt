package com.accompany.purchaseManagement

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {
    fun savePurchaseRequest(data: Map<String, Any>, onSuccess: (() -> Unit)? = null, onFailure: ((Exception) -> Unit)? = null) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").add(data)
            .addOnSuccessListener { onSuccess?.invoke() }
            .addOnFailureListener { ex -> onFailure?.invoke(ex) }
    }
}
