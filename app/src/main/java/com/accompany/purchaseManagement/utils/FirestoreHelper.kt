package com.accompany.purchaseManagement.utils

import android.util.Log
import com.accompany.purchaseManagement.PurchaseManagementApp
import com.accompany.purchaseManagement.data.models.Livestock
import com.accompany.purchaseManagement.data.models.PurchaseRequest
import com.accompany.purchaseManagement.data.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Firestore 데이터베이스 작업을 위한 헬퍼 클래스
 */
class FirestoreHelper {
    
    companion object {
        private const val TAG = "FirestoreHelper"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN)
        
        @Volatile
        private var INSTANCE: FirestoreHelper? = null
        
        fun getInstance(): FirestoreHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreHelper().also { INSTANCE = it }
            }
        }
    }
    
    private val firestore: FirebaseFirestore = PurchaseManagementApp.firestore
    
    // === User Management ===
    
    /**
     * 사용자 정보 저장
     */
    suspend fun saveUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userRef = firestore.collection(PurchaseManagementApp.USERS_COLLECTION).document(user.id)
            userRef.set(user.toFirestoreMap()).await()
            
            Log.d(TAG, "User saved successfully: ${user.id}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user: ${user.id}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 사용자 정보 조회
     */
    suspend fun getUser(userId: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection(PurchaseManagementApp.USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                Log.d(TAG, "User retrieved successfully: $userId")
                Result.success(user)
            } else {
                Log.d(TAG, "User not found: $userId")
                Result.success(null)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user: $userId", e)
            Result.failure(e)
        }
    }
    
    /**
     * 이메일로 사용자 조회
     */
    suspend fun getUserByEmail(email: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(PurchaseManagementApp.USERS_COLLECTION)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            val user = querySnapshot.documents.firstOrNull()?.toObject(User::class.java)
            Log.d(TAG, "User retrieved by email: $email")
            Result.success(user)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user by email: $email", e)
            Result.failure(e)
        }
    }
    
    /**
     * 모든 사용자 조회 (관리자용)
     */
    suspend fun getAllUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(PurchaseManagementApp.USERS_COLLECTION)
                .orderBy("name")
                .get()
                .await()
            
            val users = querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }
            Log.d(TAG, "Retrieved ${users.size} users")
            Result.success(users)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all users", e)
            Result.failure(e)
        }
    }
    
    // === Purchase Request Management ===
    
    /**
     * 구매신청 저장
     */
    suspend fun savePurchaseRequest(request: PurchaseRequest): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestRef = if (request.id.isNotEmpty()) {
                firestore.collection(PurchaseManagementApp.PURCHASE_REQUESTS_COLLECTION).document(request.id)
            } else {
                firestore.collection(PurchaseManagementApp.PURCHASE_REQUESTS_COLLECTION).document()
            }
            
            val requestWithId = request.copy(
                id = requestRef.id,
                requestDate = if (request.requestDate.isEmpty()) {
                    dateFormat.format(Date())
                } else {
                    request.requestDate
                }
            )
            
            requestRef.set(requestWithId.toFirestoreMap()).await()
            
            Log.d(TAG, "Purchase request saved successfully: ${requestRef.id}")
            Result.success(requestRef.id)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save purchase request", e)
            Result.failure(e)
        }
    }
    
    /**
     * 구매신청 조회
     */
    suspend fun getPurchaseRequest(requestId: String): Result<PurchaseRequest?> = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection(PurchaseManagementApp.PURCHASE_REQUESTS_COLLECTION)
                .document(requestId)
                .get()
                .await()
            
            if (document.exists()) {
                val request = document.toObject(PurchaseRequest::class.java)
                Log.d(TAG, "Purchase request retrieved successfully: $requestId")
                Result.success(request)
            } else {
                Log.d(TAG, "Purchase request not found: $requestId")
                Result.success(null)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get purchase request: $requestId", e)
            Result.failure(e)
        }
    }
    
    /**
     * 사용자별 구매신청 목록 조회
     */
    suspend fun getPurchaseRequestsByUser(userId: String, limit: Int = 50): Result<List<PurchaseRequest>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(PurchaseManagementApp.PURCHASE_REQUESTS_COLLECTION)
                .whereEqualTo("applicantId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val requests = querySnapshot.documents.mapNotNull { it.toObject(PurchaseRequest::class.java) }
            Log.d(TAG, "Retrieved ${requests.size} purchase requests for user: $userId")
            Result.success(requests)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get purchase requests for user: $userId", e)
            Result.failure(e)
        }
    }
    
    /**
     * 모든 구매신청 목록 조회 (관리자용)
     */
    suspend fun getAllPurchaseRequests(limit: Int = 100): Result<List<PurchaseRequest>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(PurchaseManagementApp.PURCHASE_REQUESTS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val requests = querySnapshot.documents.mapNotNull { it.toObject(PurchaseRequest::class.java) }
            Log.d(TAG, "Retrieved ${requests.size} purchase requests")
            Result.success(requests)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all purchase requests", e)
            Result.failure(e)
        }
    }
    
    /**
     * 구매신청 상태 업데이트
     */
    suspend fun updatePurchaseRequestStatus(
        requestId: String,
        status: String,
        processor: String? = null,
        processNote: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "processedDate" to dateFormat.format(Date()),
                "updatedAt" to System.currentTimeMillis()
            )
            
            processor?.let { updates["processor"] = it }
            processNote?.let { updates["processNote"] = it }
            
            firestore.collection(PurchaseManagementApp.PURCHASE_REQUESTS_COLLECTION)
                .document(requestId)
                .update(updates)
                .await()
            
            Log.d(TAG, "Purchase request status updated: $requestId -> $status")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update purchase request status: $requestId", e)
            Result.failure(e)
        }
    }
    
    // === Livestock Management ===
    
    /**
     * 가축 정보 저장
     */
    suspend fun saveLivestock(livestock: Livestock): Result<String> = withContext(Dispatchers.IO) {
        try {
            val livestockRef = if (livestock.id.isNotEmpty()) {
                firestore.collection(PurchaseManagementApp.LIVESTOCK_COLLECTION).document(livestock.id)
            } else {
                firestore.collection(PurchaseManagementApp.LIVESTOCK_COLLECTION).document()
            }
            
            val livestockWithId = livestock.copy(
                id = livestockRef.id,
                registrationDate = if (livestock.registrationDate.isEmpty()) {
                    dateFormat.format(Date())
                } else {
                    livestock.registrationDate
                }
            )
            
            livestockRef.set(livestockWithId.toFirestoreMap()).await()
            
            Log.d(TAG, "Livestock saved successfully: ${livestockRef.id}")
            Result.success(livestockRef.id)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save livestock", e)
            Result.failure(e)
        }
    }
    
    /**
     * 가축 정보 조회
     */
    suspend fun getLivestock(livestockId: String): Result<Livestock?> = withContext(Dispatchers.IO) {
        try {
            val document = firestore.collection(PurchaseManagementApp.LIVESTOCK_COLLECTION)
                .document(livestockId)
                .get()
                .await()
            
            if (document.exists()) {
                val livestock = document.toObject(Livestock::class.java)
                Log.d(TAG, "Livestock retrieved successfully: $livestockId")
                Result.success(livestock)
            } else {
                Log.d(TAG, "Livestock not found: $livestockId")
                Result.success(null)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get livestock: $livestockId", e)
            Result.failure(e)
        }
    }
    
    /**
     * 모든 가축 정보 조회
     */
    suspend fun getAllLivestock(limit: Int = 100): Result<List<Livestock>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(PurchaseManagementApp.LIVESTOCK_COLLECTION)
                .whereEqualTo("isActive", true)
                .orderBy("registrationDate", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val livestockList = querySnapshot.documents.mapNotNull { it.toObject(Livestock::class.java) }
            Log.d(TAG, "Retrieved ${livestockList.size} livestock records")
            Result.success(livestockList)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all livestock", e)
            Result.failure(e)
        }
    }
    
    // === Utility Functions ===
    
    /**
     * 문서 삭제
     */
    suspend fun deleteDocument(collection: String, documentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(collection).document(documentId).delete().await()
            Log.d(TAG, "Document deleted successfully: $collection/$documentId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete document: $collection/$documentId", e)
            Result.failure(e)
        }
    }
    
    /**
     * 배치 쓰기 작업
     */
    suspend fun executeBatch(operations: suspend (FirebaseFirestore) -> Unit): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            operations(firestore)
            Log.d(TAG, "Batch operation completed successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Batch operation failed", e)
            Result.failure(e)
        }
    }
}