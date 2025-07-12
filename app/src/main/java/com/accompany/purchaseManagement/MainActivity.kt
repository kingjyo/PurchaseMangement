package com.accompany.purchaseManagement

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

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

    private var currentUser: GoogleAuthHelper.UserInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 로그인 체크
        googleAuthHelper = GoogleAuthHelper(this)
        if (!googleAuthHelper.isLoggedIn()) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        currentUser = googleAuthHelper.getCurrentUser()
        dbHelper = PurchaseRequestDbHelper(this)
        fcmHelper = FcmNotificationHelper(this)

        initViews()
        setupButtons()
        updateWelcomeMessage()

        // FCM 토큰 업데이트
        lifecycleScope.launch {
            currentUser?.email?.let { email ->
                fcmHelper.updateFcmToken(email)
            }
        }

        // 자동 데이터 정리
        autoCleanOldData()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnPurchaseRequest = findViewById(R.id.btnPurchaseRequest)
        btnPurchaseStatus = findViewById(R.id.btnPurchaseStatus)
        btnPurchaseHistory = findViewById(R.id.btnPurchaseHistory)
        btnCattleStatus = findViewById(R.id.btnCattleStatus)
        btnAdmin = findViewById(R.id.btnAdmin)
    }

    private fun setupButtons() {
        // 구매신청 버튼
        btnPurchaseRequest.setOnClickListener {
            if (currentUser?.name == "미설정" || currentUser?.department == "미설정") {
                showProfileSetupDialog()
            } else {
                val intent = Intent(this, PurchaseRequestActivity::class.java)
                startActivity(intent)
            }
        }

        // 구매신청 현황 버튼
        btnPurchaseStatus.setOnClickListener {
            val intent = Intent(this, PurchaseStatusActivity::class.java)
            startActivity(intent)
        }

        // 구매신청 기록 버튼
        btnPurchaseHistory.setOnClickListener {
            val intent = Intent(this, PurchaseHistoryActivity::class.java)
            startActivity(intent)
        }

        // 축우현황 버튼
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

    private fun updateWelcomeMessage() {
        currentUser?.let { user ->
            val welcomeText = if (user.name != "미설정" && user.department != "미설정") {
                "${user.name}님 (${user.department})"
            } else {
                "환영합니다! 👆 프로필을 설정해주세요"
            }
            tvWelcome.text = welcomeText
        }
    }

    private fun showProfileSetupDialog() {
        AlertDialog.Builder(this)
            .setTitle("프로필 설정 필요")
            .setMessage("구매신청을 하려면 이름과 소속을 설정해야 합니다.\n관리자에게 문의해주세요.")
            .setPositiveButton("확인", null)
            .show()
    }

    private fun autoCleanOldData() {
        val deletedCount = dbHelper.deleteOldRecords()
        if (deletedCount > 0 && currentUser?.isAdmin == true) {
            Toast.makeText(this, "${deletedCount}개의 오래된 기록이 정리되었습니다", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://sheets.google.com")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "브라우저를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStatistics() {
        lifecycleScope.launch {
            try {
                // TODO: Firestore에서 통계 데이터 가져오기
                val totalCount = 100 // 예시
                val pendingCount = 20
                val completedCount = 80

                val message = """
                    📊 구매신청 통계
                    
                    총 신청: ${totalCount}건
                    대기중: ${pendingCount}건
                    완료: ${completedCount}건
                    
                    완료율: ${(completedCount * 100 / totalCount)}%
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

// MainActivity_Part1.kt에서 이어서...

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                showProfileDialog()
                true
            }
            R.id.action_logout -> {
                showLogoutConfirm()
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

    private fun performLogout() {
        googleAuthHelper.signOut {
            Toast.makeText(this, "로그아웃되었습니다", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // 다른 화면에서 돌아왔을 때 사용자 정보 갱신
        currentUser = googleAuthHelper.getCurrentUser()
        updateWelcomeMessage()
    }
}

// MainActivity 전체 코드를 합치려면:
// 1. MainActivity_Part1.kt의 내용을 복사
// 2. "// MainActivity_Part2.kt에서 계속..." 부분을 삭제
// 3. MainActivity_Part2.kt의 내용을 이어서 붙여넣기
// 4. 맨 마지막 중괄호 } 확인
