package com.accompany.purchaseManagement

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PurchaseManagementApp : Application() {
    
    companion object {
        private const val TAG = "PurchaseManagementApp"
        
        // Application scope for coroutines
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        
        // Firebase 인스턴스들
        lateinit var auth: FirebaseAuth
            private set
        
        lateinit var firestore: FirebaseFirestore
            private set
        
        lateinit var storage: FirebaseStorage
            private set
        
        lateinit var messaging: FirebaseMessaging
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
        const val PURCHASE_REQUEST_IMAGES_FOLDER = "purchase_request_images"
        
        // 사용자 역할
        const val ROLE_ADMIN = "admin"
        const val ROLE_MANAGER = "manager"
        const val ROLE_USER = "user"
        
        // SharedPreferences keys
        const val PREFS_NAME = "UserPrefs"
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_USER_ID = "userId"
        const val KEY_USER_NAME = "userName"
        const val KEY_USER_EMAIL = "userEmail"
        const val KEY_USER_DEPARTMENT = "userDepartment"
        const val KEY_USER_ROLE = "userRole"
        const val KEY_FCM_TOKEN = "fcmToken"
        
        // 앱 인스턴스
        private lateinit var instance: PurchaseManagementApp
        
        fun getInstance(): PurchaseManagementApp = instance
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        try {
            // Firebase 초기화
            initializeFirebase()
            
            // FCM 토큰 갱신
            updateFCMToken()
            
            // 전역 예외 처리기 설정
            setupGlobalExceptionHandler()
            
            Log.d(TAG, "Application initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize application", e)
        }
    }
    
    private fun initializeFirebase() {
        try {
            // Firebase 앱 초기화
            FirebaseApp.initializeApp(this)
            
            // Firebase 서비스 인스턴스 초기화
            auth = FirebaseAuth.getInstance().apply {
                // 한국어 설정
                setLanguageCode("ko")
            }
            
            firestore = Firebase.firestore.apply {
                // Firestore 설정
                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true) // 오프라인 지원
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) // 무제한 캐시
                    .build()
                firestoreSettings = settings
            }
            
            storage = Firebase.storage.apply {
                // Storage 설정
                maxUploadRetryTimeMillis = 120000 // 2분
                maxDownloadRetryTimeMillis = 120000 // 2분
            }
            
            messaging = FirebaseMessaging.getInstance()
            
            Log.d(TAG, "Firebase services initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase", e)
            throw e
        }
    }
    
    private fun updateFCMToken() {
        applicationScope.launch {
            try {
                messaging.token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d(TAG, "FCM Token: $token")
                        
                        // SharedPreferences에 토큰 저장
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                            .putString(KEY_FCM_TOKEN, token)
                            .apply()
                        
                        // 로그인된 사용자가 있으면 Firestore에도 업데이트
                        auth.currentUser?.let { user ->
                            firestore.collection(USERS_COLLECTION)
                                .document(user.uid)
                                .update("fcmToken", token)
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to update FCM token in Firestore", e)
                                }
                        }
                    } else {
                        Log.e(TAG, "Failed to get FCM token", task.exception)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating FCM token", e)
            }
        }
    }
    
    private fun setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            
            // 크래시 정보를 Firestore에 저장 (선택사항)
            try {
                val crashInfo = hashMapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "thread" to thread.name,
                    "exception" to throwable.toString(),
                    "stackTrace" to throwable.stackTraceToString(),
                    "userId" to (auth.currentUser?.uid ?: "anonymous"),
                    "appVersion" to packageManager.getPackageInfo(packageName, 0).versionName
                )
                
                firestore.collection("crash_logs")
                    .add(crashInfo)
                    .addOnSuccessListener {
                        Log.d(TAG, "Crash log saved to Firestore")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save crash log", e)
            }
            
            // 기본 예외 처리기 호출
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }
    }
    
    fun clearUserSession() {
        // SharedPreferences 초기화
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply()
        
        // Firebase Auth 로그아웃
        auth.signOut()
        
        Log.d(TAG, "User session cleared")
    }
    
    fun saveUserSession(userId: String, name: String, email: String, department: String, role: String) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_DEPARTMENT, department)
            putString(KEY_USER_ROLE, role)
            apply()
        }
        
        Log.d(TAG, "User session saved: $name ($email)")
    }
}