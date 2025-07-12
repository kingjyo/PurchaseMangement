package com.accompany.purchaseManagement

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FcmNotificationHelper(private val context: Context) {

    companion object {
        private const val TAG = "FcmNotificationHelper"

        // 알림 타입
        const val TYPE_NEW_REQUEST = "new_request"           // 새 구매신청 (관리자용)
        const val TYPE_STATUS_CHANGED = "status_changed"     // 상태 변경 (신청자용)
        const val TYPE_REQUEST_MODIFIED = "request_modified" // 신청 수정 (관리자용)
    }

    private val db = FirebaseFirestore.getInstance()

    // FCM 토큰 업데이트
    suspend fun updateFcmToken(userEmail: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()

            db.collection("users").document(userEmail)
                .update("fcmToken", token)
                .await()

            Log.d(TAG, "FCM 토큰 업데이트 성공: $userEmail")
        } catch (e: Exception) {
            Log.e(TAG, "FCM 토큰 업데이트 실패", e)
        }
    }

    // 관리자에게 새 구매신청 알림
    suspend fun notifyAdminNewRequest(
        applicantName: String,
        equipmentName: String,
        requestId: String
    ) {
        try {
            // 관리자 정보 조회
            val adminDoc = db.collection("users")
                .document(AppConfig.MANAGER_EMAIL)
                .get()
                .await()

            val adminToken = adminDoc.getString("fcmToken")
            if (adminToken.isNullOrEmpty()) {
                Log.w(TAG, "관리자 FCM 토큰이 없습니다")
                return
            }

            // Cloud Functions를 통한 알림 전송 요청
            val notificationData = hashMapOf(
                "type" to TYPE_NEW_REQUEST,
                "targetToken" to adminToken,
                "title" to "🛒 새 구매신청",
                "body" to "${applicantName}님이 $equipmentName 구매를 신청했습니다",
                "requestId" to requestId,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("notifications")
                .add(notificationData)
                .await()

            Log.d(TAG, "관리자 알림 요청 생성됨")

        } catch (e: Exception) {
            Log.e(TAG, "관리자 알림 전송 실패", e)
        }
    }

    // 신청자에게 상태 변경 알림
    suspend fun notifyRequesterStatusChanged(
        requesterEmail: String,
        equipmentName: String,
        oldStatus: String,
        newStatus: String,
        requestId: String
    ) {
        try {
            // 신청자 정보 조회
            val userDoc = db.collection("users")
                .document(requesterEmail)
                .get()
                .await()

            val userToken = userDoc.getString("fcmToken")
            if (userToken.isNullOrEmpty()) {
                Log.w(TAG, "신청자 FCM 토큰이 없습니다: $requesterEmail")
                return
            }

            // 상태별 이모지
            val statusEmoji = when (newStatus) {
                "확인완료" -> "✅"
                "결재중" -> "📋"
                "결재완료" -> "✔️"
                "선조치" -> "🚀"
                "완료" -> "🎉"
                else -> "📌"
            }

            // Cloud Functions를 통한 알림 전송 요청
            val notificationData = hashMapOf(
                "type" to TYPE_STATUS_CHANGED,
                "targetToken" to userToken,
                "title" to "$statusEmoji 구매신청 상태 변경",
                "body" to "$equipmentName: $oldStatus → $newStatus",
                "requestId" to requestId,
                "oldStatus" to oldStatus,
                "newStatus" to newStatus,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("notifications")
                .add(notificationData)
                .await()

            Log.d(TAG, "신청자 알림 요청 생성됨: $requesterEmail")

        } catch (e: Exception) {
            Log.e(TAG, "신청자 알림 전송 실패", e)
        }
    }

    // 관리자에게 신청 수정 알림
    suspend fun notifyAdminRequestModified(
        applicantName: String,
        equipmentName: String,
        modifiedFields: List<String>,
        requestId: String
    ) {
        try {
            // 관리자 정보 조회
            val adminDoc = db.collection("users")
                .document(AppConfig.MANAGER_EMAIL)
                .get()
                .await()

            val adminToken = adminDoc.getString("fcmToken")
            if (adminToken.isNullOrEmpty()) {
                Log.w(TAG, "관리자 FCM 토큰이 없습니다")
                return
            }

            val fieldsText = modifiedFields.joinToString(", ")

            // Cloud Functions를 통한 알림 전송 요청
            val notificationData = hashMapOf(
                "type" to TYPE_REQUEST_MODIFIED,
                "targetToken" to adminToken,
                "title" to "📝 구매신청 수정됨",
                "body" to "${applicantName}님이 $equipmentName 신청을 수정했습니다 ($fieldsText)",
                "requestId" to requestId,
                "modifiedFields" to modifiedFields,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("notifications")
                .add(notificationData)
                .await()

            Log.d(TAG, "관리자 수정 알림 요청 생성됨")

        } catch (e: Exception) {
            Log.e(TAG, "관리자 수정 알림 전송 실패", e)
        }
    }

    // 알림 권한 요청 (Android 13+)
    fun requestNotificationPermission(activity: android.app.Activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (activity.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}