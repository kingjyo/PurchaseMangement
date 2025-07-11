package com.accompany.purchaseManagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.accompany.purchaseManagement.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore
import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import android.provider.Settings

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: PurchaseRequestDbHelper
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 디바이스 ID 얻기
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Log.d("DeviceID", "현재 디바이스 ID: $deviceId")

        // 관리자 디바이스 ID와 비교하여 토큰 등록
        if (deviceId == "6e245082df66e4f9") {
            registerAdminFcmToken()
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener

            }
            // FCM 토큰 얻기
            val token = task.result
            Log.d(TAG, "FCM Token: $token")

            // 이후 token을 Firestore에 저장하거나, 서버로 전송할 수 있음
        }

        dbHelper = PurchaseRequestDbHelper(this)

        setupButtons()
        autoCleanOldData()
    }

    private fun setupButtons() {
        // 구매신청 버튼
        val btnPurchaseRequest: Button = findViewById(R.id.btnPurchaseRequest)
        btnPurchaseRequest.setOnClickListener {
            val intent = Intent(this, PurchaseRequestActivity::class.java)
            startActivity(intent)
        }

        // 구매신청 현황 버튼
        val btnPurchaseStatus: Button = findViewById(R.id.btnPurchaseStatus)
        btnPurchaseStatus.setOnClickListener {
            val intent = Intent(this, PurchaseStatusActivity::class.java)
            startActivity(intent)
        }

        // 구매신청 기록 버튼
        val btnPurchaseHistory: Button = findViewById(R.id.btnPurchaseHistory)
        btnPurchaseHistory.setOnClickListener {
            val intent = Intent(this, PurchaseHistoryActivity::class.java)
            startActivity(intent)
        }

        val btnCattleStatus = findViewById<Button>(R.id.btnCattleStatus)
        btnCattleStatus.setOnClickListener {
            val intent = Intent(this, CattleStatusActivity::class.java)
            startActivity(intent)
        }

        // 관리자 메뉴 버튼
        val btnAdmin: Button = findViewById(R.id.btnAdmin)
        btnAdmin.setOnClickListener {
            showAdminMenu()
        }

        // Google Sheets 연결 테스트 (관리자 버튼 길게 누르기)
        btnAdmin.setOnLongClickListener {
            testGoogleSheetsConnection()
            true
        }
    }

    private fun autoCleanOldData() {
        val deletedCount = dbHelper.deleteOldRecords()
        if (deletedCount > 0) {
            Toast.makeText(this, "${deletedCount}개의 오래된 기록이 정리되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerAdminFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val db = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "fcmToken" to token,
                    "role" to "admin"
                )
                db.collection("users").document("admin")
                    .set(data)
                    .addOnSuccessListener {
                        // 저장 성공 (원하면 Toast, Log 추가)
                    }
                    .addOnFailureListener {
                        // 저장 실패 (원하면 Toast, Log 추가)
                    }
            }
            .addOnFailureListener {
                // 토큰 획득 실패 (원하면 Toast, Log 추가)
            }
    }

    private fun testGoogleSheetsConnection() {
        Toast.makeText(this, "Google Sheets 연결 테스트 중...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val googleSheetsHelper = GoogleSheetsHelper(this@MainActivity)
            val isConnected = googleSheetsHelper.testConnection()

            val message = if (isConnected) {
                "✅ Google Sheets 연결 성공!\n실시간 연동이 정상 작동합니다."
            } else {
                "❌ Google Sheets 연결 실패!\nAppConfig.kt에서 URL 설정을 확인해주세요."
            }

            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showAdminMenu() {
        val recordCount = dbHelper.getRecordCount()
        val oldestDate = dbHelper.getOldestRecordDate()

        val message = """
            📊 로컬 저장된 기록: ${recordCount}개
            📅 가장 오래된 기록: ${oldestDate?.let {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
                val outputFormat = SimpleDateFormat("MM/dd", Locale.KOREA)
                val date = inputFormat.parse(it)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                it.substring(0, 10)
            }
        } ?: "없음"}
            
            💡 팁: 관리자 버튼을 길게 누르면 Google Sheets 연결 테스트
        """.trimIndent()

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("🔧 관리자 메뉴")
        builder.setMessage(message)

        builder.setPositiveButton("📊 Google Sheets 열기") { _, _ ->
            openGoogleSheets()
        }

        builder.setNeutralButton("📁 로컬 기록 보기") { _, _ ->
            val intent = Intent(this, PurchaseHistoryActivity::class.java)
            startActivity(intent)
        }

        builder.setNegativeButton("🗑️ 로컬 데이터 초기화") { _, _ ->
            showDataDeleteConfirm()
        }

        builder.show()
    }

    private fun openGoogleSheets() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("📊 Google Sheets 접속")
        builder.setMessage("""
            Google Sheets에서 실시간 구매신청 현황을 확인하세요!
            
            📱 방법 1: 브라우저에서 직접 접속
            sheets.google.com → "구매신청 관리시스템" 찾기
            
            📱 방법 2: Google Sheets 앱 사용
            앱스토어에서 "Google Sheets" 다운로드
            
            💡 북마크 추천: 자주 사용하는 경우 브라우저에 북마크 저장
        """.trimIndent())

        builder.setPositiveButton("브라우저로 열기") { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://sheets.google.com")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "브라우저를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("취소", null)
        builder.show()
    }

    private fun showDataDeleteConfirm() {
        val recordCount = dbHelper.getRecordCount()

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("⚠️ 로컬 데이터 초기화 확인")
        builder.setMessage("""
            로컬에 저장된 ${recordCount}개의 구매신청 기록이 삭제됩니다.
            
            ⚠️ Google Sheets의 데이터는 삭제되지 않습니다.
            ⚠️ 이 작업은 되돌릴 수 없습니다.
            
            정말 삭제하시겠습니까?
        """.trimIndent())

        builder.setPositiveButton("삭제") { _, _ ->
            val success = dbHelper.deleteAllRecords()
            if (success) {
                Toast.makeText(this, "로컬 데이터가 삭제되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "삭제 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("취소", null)
        builder.show()
    }
}
