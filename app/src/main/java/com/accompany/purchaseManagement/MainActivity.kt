package com.accompany.purchaseManagement

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

// 네이버 로그인 라이브러리가 없다면 이 부분 전체 주석처리/삭제 가능
// import com.naver.android.nlogin.OAuthLogin
// import com.naver.android.nlogin.OAuthLoginHandler
// import com.naver.android.nlogin.widget.OAuthLoginButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: PurchaseRequestDbHelper
    private lateinit var googleAuthHelper: GoogleAuthHelper
    private lateinit var fcmHelper: FcmNotificationHelper

    // UI 요소들
    private lateinit var tvWelcome: TextView
    private lateinit var btnPurchaseRequest: Button
    private lateinit var btnPurchaseStatus: Button
    private lateinit var btnPurchaseHistory: Button
    private lateinit var btnCattleStatus: Button
    private lateinit var btnAdmin: Button
    // private lateinit var naverLoginButton: OAuthLoginButton  // 네이버 로그인 버튼 (주석)

    private var currentUser: UserInfo? = null // 반드시 존재해야 함!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 로그인 체크 (SharedPreferences를 이용)
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false) // 로그인 상태 확인

        if (!isLoggedIn) {
            // 로그인되지 않으면 LoginActivity로 이동
            navigateToLogin()
            return
        }

        googleAuthHelper = GoogleAuthHelper(this)
        currentUser = googleAuthHelper.getCurrentUser()
        dbHelper = PurchaseRequestDbHelper(this)
        fcmHelper = FcmNotificationHelper(this)

        initViews()
        setupButtons()
        updateWelcomeMessage(currentUser?.name)

        // (선택) 네이버 로그인 버튼은 실제 구현 시 여기에 추가
        /*
        mOAuthLoginModule = OAuthLogin.getInstance()
        mOAuthLoginModule.init(this, "YOUR_CLIENT_ID", "YOUR_CLIENT_SECRET", "YOUR_REDIRECT_URI")
        naverLoginButton = findViewById(R.id.naverLoginButton)
        naverLoginButton.setOAuthLoginHandler(object : OAuthLoginHandler() {
            override fun run(success: Boolean) {
                // 네이버 로그인 로직
            }
        })
        */

        // FCM 토큰 업데이트
        lifecycleScope.launch {
            currentUser?.email?.let { email ->
                fcmHelper.updateFcmToken(email)
            }
        }

        // 자동 데이터 정리
        autoCleanOldData()
    }

    override fun onResume() {
        super.onResume()

        // 항상 최신 currentUser 정보로 재초기화
        googleAuthHelper = GoogleAuthHelper(this)
        currentUser = googleAuthHelper.getCurrentUser()

        if (currentUser == null) {
            navigateToLogin()
            return
        }

        // 프로필 미설정 시 프로필 입력 화면으로 이동
        if (currentUser?.name == "미설정" || currentUser?.department == "미설정") {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
            finish()
            return
        }

        // UI 최신화 (환영 메시지 등)
        updateWelcomeMessage(currentUser?.name)
        // 필요하면 뷰 초기화나 버튼 세팅도 여기에!
    }

    private fun updateWelcomeMessage(userName: String?) {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvWelcome.text = "${userName ?: "사용자"}님, 환영합니다!"  // 사용자 이름을 환영 메시지로 표시
    }

    private fun showProfileSetupDialog() {
        AlertDialog.Builder(this)
            .setTitle("프로필 설정 필요")
            .setMessage("구매신청을 하려면 이름과 소속을 설정해야 합니다.\n관리자에게 문의해주세요.")
            .setPositiveButton("확인", null)
            .show()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnPurchaseRequest = findViewById(R.id.btnPurchaseRequest)
        btnPurchaseStatus = findViewById(R.id.btnPurchaseStatus)
        btnPurchaseHistory = findViewById(R.id.btnPurchaseHistory)
        btnCattleStatus = findViewById(R.id.btnCattleStatus)
        btnAdmin = findViewById(R.id.btnAdmin)
        // naverLoginButton = findViewById(R.id.naverLoginButton) // 네이버 로그인 버튼 (옵션)
    }

    private fun setupButtons() {
        // 구매신청 버튼
        btnPurchaseRequest.setOnClickListener {
            if (currentUser?.name == "미설정" || currentUser?.department == "미설정") {
                showProfileSetupDialog()
            } else {
                val intent = Intent(this, PurchaseRequestActivityV2::class.java)
                startActivity(intent)
            }
        }

        btnPurchaseStatus.setOnClickListener {
            val intent = Intent(this, PurchaseStatusActivityV2::class.java)
            startActivity(intent)
        }

        btnPurchaseHistory.setOnClickListener {
            val intent = Intent(this, PurchaseHistoryActivity::class.java)
            startActivity(intent)
        }

        btnCattleStatus.setOnClickListener {
            val intent = Intent(this, CattleStatusActivity::class.java)
            startActivity(intent)
        }

        // 관리자 버튼 (관리자만 표시)
        btnAdmin.visibility = if (currentUser?.isAdmin == true) View.VISIBLE else View.GONE
        btnAdmin.setOnClickListener {
            showAdminMenu()
        }
    }

    private fun showAdminMenu() {
        val options = arrayListOf(
            "👥 사용자 관리",
            "📊 Google Sheets 열기",
            "📈 통계 보기",
            "🗑️ 로컬 데이터 초기화"
        )

        AlertDialog.Builder(this)
            .setTitle("🔧 관리자 메뉴")
            .setItems(options.toTypedArray()) { _, which ->
                when (which) {
                    0 -> openUserManagement()
                    1 -> openGoogleSheets()
                    2 -> showStatistics()
                    3 -> showDataDeleteConfirm()
                }
            }
            .show()
    }

    private fun openUserManagement() {
        val intent = Intent(this, UserManagementActivity::class.java)
        startActivity(intent)
    }

    private fun openGoogleSheets() {
        // AppConfig.GOOGLE_SHEETS_URL이 정의되어 있다고 가정
        val url = try { AppConfig.GOOGLE_SHEETS_URL } catch (e: Exception) { "" }
        if (url.isNotBlank()) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(url)
            startActivity(intent)
        } else {
            Toast.makeText(this, "구글 시트 URL을 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStatistics() {
        lifecycleScope.launch {
            try {
                val totalCount = dbHelper.getRecordCount()
                val pendingCount = dbHelper.getPendingCount()
                val completedCount = totalCount - pendingCount

                val message = """
                    📊 구매신청 통계

                    총 신청: ${totalCount}건
                    대기중: ${pendingCount}건
                    완료: ${completedCount}건

                    완료율: ${if (totalCount > 0) (completedCount * 100 / totalCount) else 0}%
                """.trimIndent()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("통계")
                    .setMessage(message)
                    .setPositiveButton("확인", null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "통계 로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDataDeleteConfirm() {
        val recordCount = dbHelper.getRecordCount()

        AlertDialog.Builder(this)
            .setTitle("⚠️ 로컬 데이터 초기화")
            .setMessage("로컬에 저장된 ${recordCount}개의 기록이 삭제됩니다.\n계속하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                val success = dbHelper.deleteAllRecords()
                if (success) {
                    Toast.makeText(this, "로컬 데이터가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivityV2::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()

        Toast.makeText(this, "로그아웃되었습니다", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun autoCleanOldData() {
        val deletedCount = dbHelper.deleteOldRecords()
        if (deletedCount > 0 && currentUser?.isAdmin == true) {
            Toast.makeText(this, "${deletedCount}개의 오래된 기록이 정리되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                showProfileDialog()
                true
            }
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showProfileDialog() {
        currentUser?.let { user ->
            val message = """
                👤 이름: ${user.name}
                🏢 소속: ${user.department}
                📧 이메일: ${user.email}
                🛡️ 권한: ${if (user.isAdmin) "관리자" else "일반 사용자"}
                
                ${if (user.name == "미설정" || user.department == "미설정")
                "\n⚠️ 관리자에게 프로필 설정을 요청하세요" else ""} 
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("내 프로필")
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show()
        }
    }

    private fun showLogoutConfirm() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃하시겠습니까?")
            .setPositiveButton("로그아웃") { _, _ ->
                performLogout()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
