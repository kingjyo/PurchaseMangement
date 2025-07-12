package com.accompany.purchaseManagement.config

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class FirebaseConfig : Application() {

    companion object {
        // Firebase 인스턴스들
        lateinit var auth: FirebaseAuth
            private set

        lateinit var firestore: FirebaseFirestore
            private set

        lateinit var storage: FirebaseStorage
            private set

        // Firestore 컬렉션 이름들
        const val USERS_COLLECTION = "users"
        const val PURCHASE_REQUESTS_COLLECTION = "purchase_requests"
        const val LIVESTOCK_COLLECTION = "livestock"
        const val CATEGORIES_COLLECTION = "categories"
        const val NOTIFICATIONS_COLLECTION = "notifications"

        // Storage 폴더 이름들
        const val PROFILE_IMAGES_FOLDER = "profile_images"
        const val LIVESTOCK_IMAGES_FOLDER = "livestock_images"
        const val PURCHASE_DOCUMENTS_FOLDER = "purchase_documents"

        // 사용자 역할
        const val ROLE_ADMIN = "admin"
        const val ROLE_MANAGER = "manager"
        const val ROLE_USER = "user"
    }

    override fun onCreate() {
        super.onCreate()

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        // Firebase 서비스 인스턴스 초기화
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
        storage = Firebase.storage

        // Firestore 설정
        configureFirestore()
    }

    private fun configureFirestore() {
        // Firestore 캐시 설정
        val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(-1)
            .setPersistenceEnabled(true)
            .build()

        firestore.firestoreSettings = settings
    }
}