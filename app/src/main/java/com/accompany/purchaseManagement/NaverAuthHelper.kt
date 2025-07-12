package com.accompany.purchaseManagement

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.tasks.await

class NaverAuthHelper(private val activity: Activity) {

    companion object {
        private const val TAG = "NaverAuthHelper"

        // 네이버 앱 정보 (네이버 개발자 센터에서 발급)
        private const val NAVER_CLIENT_ID = "YOUR_NAVER_CLIENT_ID"
        private const val NAVER_CLIENT_SECRET = "YOUR_NAVER_CLIENT_SECRET"
        private const val NAVER_CLIENT_NAME = "구매신청"
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val googleAuthHelper = GoogleAuthHelper(activity)

    init {
        // 네이버 로그인 SDK 초기화
        NaverIdLoginSDK.initialize(
            activity,
            NAVER_CLIENT_ID,
            NAVER_CLIENT_SECRET,
            NAVER_CLIENT_NAME
        )
    }

    // 네이버 로그인 시작
    fun signIn(onSuccess: (NaverUserInfo) -> Unit, onFailure: (String) -> Unit) {
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 로그인 성공 - 프로필 정보 요청
                getNaverProfile(onSuccess, onFailure)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorMsg = "네이버 로그인 실패: [$httpStatus] $message"
                Log.e(TAG, errorMsg)
                onFailure(errorMsg)
            }

            override fun onError(errorCode: Int, message: String) {
                val errorMsg = "네이버 로그인 오류: [$errorCode] $message"
                Log.e(TAG, errorMsg)
                onFailure(errorMsg)
            }
        }

        NaverIdLoginSDK.authenticate(activity, oauthLoginCallback)
    }

    // 네이버 프로필 정보 가져오기
    private fun getNaverProfile(
        onSuccess: (NaverUserInfo) -> Unit,
        onFailure: (String) -> Unit
    ) {
        NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                val profile = response.profile

                if (profile != null) {
                    val userInfo = NaverUserInfo(
                        id = profile.id ?: "",
                        email = profile.email ?: "",
                        name = profile.name ?: "네이버 사용자",
                        nickname = profile.nickname ?: "",
                        profileImage = profile.profileImage ?: "",
                        mobile = profile.mobile ?: ""
                    )

                    // Firebase 커스텀 토큰으로 인증
                    authenticateWithFirebase(userInfo, onSuccess, onFailure)
                } else {
                    onFailure("프로필 정보를 가져올 수 없습니다")
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                onFailure("프로필 조회 실패: $message")
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure("프로필 조회 오류: $message")
            }
        })
    }

    // Firebase 커스텀 인증
    private fun authenticateWithFirebase(
        userInfo: NaverUserInfo,
        onSuccess: (NaverUserInfo) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // 네이버 사용자를 Firebase에 등록/업데이트
        val email = if (userInfo.email.isNotEmpty()) {
            userInfo.email
        } else {
            "${userInfo.id}@naver.local" // 이메일이 없는 경우 가상 이메일 생성
        }

        // Firestore에 사용자 정보 저장
        saveUserToFirestore(userInfo, email) { success ->
            if (success) {
                // SharedPreferences에 저장
                saveUserToPreferences(userInfo, email)
                onSuccess(userInfo)
            } else {
                onFailure("사용자 정보 저장 실패")
            }
        }
    }

    // Firestore에 사용자 정보 저장
    private fun saveUserToFirestore(
        userInfo: NaverUserInfo,
        email: String,
        onComplete: (Boolean) -> Unit
    ) {
        val userData = hashMapOf(
            "uid" to "naver_${userInfo.id}",
            "email" to email,
            "name" to userInfo.name,
            "nickname" to userInfo.nickname,
            "profileImage" to userInfo.profileImage,
            "provider" to "naver",
            "department" to "미설정",
            "isAdmin" to false,
            "createdAt" to System.currentTimeMillis(),
            "lastLoginAt" to System.currentTimeMillis()
        )

        db.collection("users").document(email)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "네이버 사용자 정보 저장 성공")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "네이버 사용자 정보 저장 실패", e)
                onComplete(false)
            }
    }

    // SharedPreferences에 사용자 정보 저장
    private fun saveUserToPreferences(userInfo: NaverUserInfo, email: String) {
        val prefs = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("userEmail", email)
            putString("userName", userInfo.name)
            putString("userDepartment", "미설정")
            putString("userProvider", "naver")
            putString("userProfileImage", userInfo.profileImage)
            putBoolean("isAdmin", false)
            apply()
        }
    }

    // 로그아웃
    fun signOut(onComplete: () -> Unit) {
        // 네이버 로그아웃
        NaverIdLoginSDK.logout()

        // SharedPreferences 초기화
        val prefs = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        onComplete()
    }

    // 연동 해제 (회원 탈퇴)
    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        NidOAuthLogin().callDeleteTokenApi(object : OAuthLoginCallback {
            override fun onSuccess() {
                // Firestore에서도 삭제
                val prefs = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val email = prefs.getString("userEmail", "") ?: ""

                if (email.isNotEmpty()) {
                    db.collection("users").document(email)
                        .delete()
                        .addOnSuccessListener {
                            prefs.edit().clear().apply()
                            onComplete(true)
                        }
                        .addOnFailureListener {
                            onComplete(false)
                        }
                } else {
                    onComplete(true)
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.e(TAG, "연동 해제 실패: $message")
                onComplete(false)
            }

            override fun onError(errorCode: Int, message: String) {
                Log.e(TAG, "연동 해제 오류: $message")
                onComplete(false)
            }
        })
    }

    // 네이버 사용자 정보 데이터 클래스
    data class NaverUserInfo(
        val id: String,
        val email: String,
        val name: String,
        val nickname: String,
        val profileImage: String,
        val mobile: String
    )
}