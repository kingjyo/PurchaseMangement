package com.accompany.purchaseManagement

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.accompany.purchaseManagement.UserInfo

class GoogleAuthHelper(private val activity: Activity) {

    companion object {
        private const val TAG = "GoogleAuthHelper"
        const val RC_SIGN_IN = 9001

        // 관리자 이메일 (AppConfig에서 가져옴)
        private val ADMIN_EMAIL = AppConfig.MANAGER_EMAIL
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    // SharedPreferences 키
    private val PREF_NAME = "UserPrefs"
    private val KEY_USER_NAME = "userName"
    private val KEY_USER_DEPARTMENT = "userDepartment"
    private val KEY_USER_EMAIL = "userEmail"
    private val KEY_IS_ADMIN = "isAdmin"

    init {
        // Google Sign In 옵션 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    // Google 로그인 시작
    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // 로그인 결과 처리
    fun handleSignInResult(data: Intent?, onSuccess: (GoogleSignInAccount) -> Unit, onFailure: (String) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account, onSuccess, onFailure)
            } else {
                onFailure("Google 계정 정보를 가져올 수 없습니다")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign in failed", e)
            onFailure("로그인 실패: ${e.message}")
        }
    }

    // Firebase 인증
    private fun firebaseAuthWithGoogle(
        account: GoogleSignInAccount,
        onSuccess: (GoogleSignInAccount) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    // Firestore에서 사용자 정보 확인/저장
                    checkAndSaveUserInfo(account, onSuccess, onFailure)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    onFailure("Firebase 인증 실패: ${task.exception?.message}")
                }
            }
    }

    // 사용자 정보 확인 및 저장
    private fun checkAndSaveUserInfo(
        account: GoogleSignInAccount,
        onSuccess: (GoogleSignInAccount) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userEmail = account.email ?: ""
        val isAdmin = (userEmail == ADMIN_EMAIL)

        // Firestore에서 사용자 정보 조회
        db.collection("users").document(userEmail)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 기존 사용자
                    val userName = document.getString("name") ?: account.displayName ?: "미설정"
                    val userDepartment = document.getString("department") ?: "미설정"

                    saveUserToPreferences(userEmail, userName, userDepartment, isAdmin)
                    onSuccess(account)
                } else {
                    // 신규 사용자 - 기본값으로 저장
                    val newUserData = hashMapOf(
                        "email" to userEmail,
                        "name" to (account.displayName ?: "미설정"),
                        "department" to "미설정",
                        "isAdmin" to isAdmin,
                        "createdAt" to System.currentTimeMillis()
                    )

                    db.collection("users").document(userEmail)
                        .set(newUserData)
                        .addOnSuccessListener {
                            saveUserToPreferences(
                                userEmail,
                                account.displayName ?: "미설정",
                                "미설정",
                                isAdmin
                            )
                            onSuccess(account)
                        }
                        .addOnFailureListener { e ->
                            onFailure("사용자 정보 저장 실패: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                onFailure("사용자 정보 조회 실패: ${e.message}")
            }
    }

    // SharedPreferences에 사용자 정보 저장
    private fun saveUserToPreferences(email: String, name: String, department: String, isAdmin: Boolean) {
        val prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_DEPARTMENT, department)
            putBoolean(KEY_IS_ADMIN, isAdmin)
            apply()
        }

        Log.d(TAG, "사용자 정보 저장됨: $name ($department) - 관리자: $isAdmin")
    }

    // 현재 로그인된 사용자 정보 가져오기
    fun getCurrentUser(): UserInfo? {
        val prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null

        return UserInfo(
            email = email,
            name = prefs.getString(KEY_USER_NAME, "미설정") ?: "미설정",
            department = prefs.getString(KEY_USER_DEPARTMENT, "미설정") ?: "미설정",
            isAdmin = prefs.getBoolean(KEY_IS_ADMIN, false)
        )
    }

    // 로그인 상태 확인
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null && getCurrentUser() != null
    }

    // 로그아웃
    fun signOut(onComplete: () -> Unit) {
        // Firebase 로그아웃
        auth.signOut()

        // Google 로그아웃
        googleSignInClient.signOut().addOnCompleteListener {
            // SharedPreferences 초기화
            val prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            onComplete()
        }
    }

    // 사용자 정보 업데이트 (관리자용)
    fun updateUserInfo(email: String, name: String, department: String, onComplete: (Boolean) -> Unit) {
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "department" to department,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users").document(email)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "사용자 정보 업데이트 성공: $email")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "사용자 정보 업데이트 실패", e)
                onComplete(false)
            }
    }

}