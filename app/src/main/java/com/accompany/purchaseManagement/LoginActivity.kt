package com.accompany.purchaseManagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.SignInButton
import kotlinx.coroutines.launch
import com.accompany.purchaseManagement.UserInfo
import com.accompany.purchaseManagement.NaverAuthHelper.NaverUserInfo

class LoginActivityV2 : AppCompatActivity() {

    private lateinit var btnGoogleSignIn: SignInButton
    private lateinit var btnNaverSignIn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvAppTitle: TextView
    private lateinit var tvDescription: TextView

    private lateinit var googleAuthHelper: GoogleAuthHelper
    private lateinit var naverAuthHelper: NaverAuthHelper
    private lateinit var fcmHelper: FcmNotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 이미 로그인되어 있는지 확인
        if (checkExistingLogin()) {
            navigateToMain()  // 이미 로그인되어 있으면 MainActivity로 바로 이동
            return
        }

        setContentView(R.layout.activity_login)

        googleAuthHelper = GoogleAuthHelper(this)
        naverAuthHelper = NaverAuthHelper(this)
        fcmHelper = FcmNotificationHelper(this)

        initViews()
        setupClickListeners()
    }

    private fun checkExistingLogin(): Boolean {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val email = prefs.getString("userEmail", null) // 이메일 정보 확인
        return !email.isNullOrEmpty() // 이메일이 있으면 로그인된 상태
    }

    private fun initViews() {
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        btnNaverSignIn = findViewById(R.id.btnNaverSignIn)
        progressBar = findViewById(R.id.progressBar)
        tvAppTitle = findViewById(R.id.tvAppTitle)
        tvDescription = findViewById(R.id.tvDescription)

        // Google 로그인 버튼 텍스트 커스터마이징
        for (i in 0 until btnGoogleSignIn.childCount) {
            val v = btnGoogleSignIn.getChildAt(i)
            if (v is TextView) {
                v.text = "Google 계정으로 로그인"
                v.textSize = 18f
                break
            }
        }
    }

    private fun setupClickListeners() {
        // Google 로그인
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // 네이버 로그인
        btnNaverSignIn.setOnClickListener {
            signInWithNaver()
        }
    }

    private fun signInWithGoogle() {
        setButtonsEnabled(false)
        progressBar.visibility = View.VISIBLE

        googleAuthHelper.signIn()
    }

    private fun signInWithNaver() {
        setButtonsEnabled(false)
        progressBar.visibility = View.VISIBLE

        naverAuthHelper.signIn(
            onSuccess = { userInfo ->
                // 로그인 성공
                lifecycleScope.launch {
                    // FCM 토큰 업데이트
                    val email = if (userInfo.email.isNotEmpty()) {
                        userInfo.email
                    } else {
                        "${userInfo.id}@naver.local"
                    }
                    fcmHelper.updateFcmToken(email)

                    // 알림 권한 요청
                    fcmHelper.requestNotificationPermission(this@LoginActivityV2)

                    // 사용자 정보를 SharedPreferences에 저장
                    val info = UserInfo(
                        email = email,
                        name = userInfo.name,
                        department = "미설정",
                        isAdmin = false
                    )
                    saveUserInfo(info)

                    Toast.makeText(
                        this@LoginActivityV2,
                        "${userInfo.name}님 환영합니다!",
                        Toast.LENGTH_SHORT
                    ).show()

                    navigateToMain()  // 로그인 후 MainActivity로 이동
                }
            },
            onFailure = { error ->
                // 로그인 실패
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                setButtonsEnabled(true)
                progressBar.visibility = View.GONE
            }
        )
    }

    private fun saveUserInfo(email: String, name: String) {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("userEmail", email)
        editor.putString("userName", name)
        editor.putBoolean("isLoggedIn", true)  // 로그인 상태를 저장
        editor.apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GoogleAuthHelper.RC_SIGN_IN) {
            googleAuthHelper.handleSignInResult(
                data,
                onSuccess = { account ->
                    // Google 로그인 성공
                    lifecycleScope.launch {
                        account.email?.let { email ->
                            fcmHelper.updateFcmToken(email)
                        }

                        fcmHelper.requestNotificationPermission(this@LoginActivityV2)

                        Toast.makeText(
                            this@LoginActivityV2,
                            "${account.displayName}님 환영합니다!",
                            Toast.LENGTH_SHORT
                        ).show()

                        navigateToMain()  // 로그인 후 MainActivity로 이동
                    }
                },
                onFailure = { error ->
                    // Google 로그인 실패
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    setButtonsEnabled(true)
                    progressBar.visibility = View.GONE
                }
            )
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        btnGoogleSignIn.isEnabled = enabled
        btnNaverSignIn.isEnabled = enabled
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
