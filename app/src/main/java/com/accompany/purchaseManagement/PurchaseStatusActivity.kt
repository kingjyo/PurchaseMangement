package com.accompany.purchaseManagement

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import com.accompany.purchaserequest.R

class PurchaseStatusActivity : AppCompatActivity() {

    private lateinit var lvPendingRequests: ListView
    private lateinit var adapter: PurchaseRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_status)

        supportActionBar?.title = "구매신청 현황"

        initViews()
        loadPendingRequestsFromApi()
    }

    private fun initViews() {
        lvPendingRequests = findViewById(R.id.lvPendingRequests)
    }

    private fun loadPendingRequestsFromApi() {
        lifecycleScope.launch {
            try {
                val requests = RetrofitClient.api.getPurchaseRequests()
                runOnUiThread {
                    adapter = PurchaseRequestAdapter(this@PurchaseStatusActivity, requests) { request ->
                        // 아이템 클릭 시 다이얼로그 보여주기
                        showStatusUpdateDialog(request)
                    }
                    lvPendingRequests.adapter = adapter
                }
            } catch (e: Exception) {
                Toast.makeText(this@PurchaseStatusActivity, "데이터 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showStatusUpdateDialog(request: PurchaseRequest) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("구매신청 처리")
        builder.setMessage("""
            ${request.applicantName}님의 구매신청을 어떻게 처리하시겠습니까?
            
            🔧 장비: ${request.applicantDepartment}
            📍 장소: ${request.location}
            📝 용도: ${request.purpose}
            
            ⚠️ 실제 승인/거부는 Google Sheets에서 처리하는 것을 권장합니다.
        """.trimIndent())

        builder.setPositiveButton("✅ 완료") { _, _ ->
            // 여기선 로컬 DB 업데이트 함수 호출하거나, 서버 API 호출할 수 있음
            // 예시: dbHelper.updateRequestStatus(request.id, "완료")
            Toast.makeText(this, "완료 처리 되었습니다", Toast.LENGTH_SHORT).show()
            loadPendingRequestsFromApi() // 목록 새로고침
        }

        builder.setNegativeButton("❌ 거부") { _, _ ->
            // 예시: dbHelper.updateRequestStatus(request.id, "거부")
            Toast.makeText(this, "거부 처리 되었습니다", Toast.LENGTH_SHORT).show()
            loadPendingRequestsFromApi()
        }

        builder.setNeutralButton("취소", null)

        builder.show()
    }

    override fun onResume() {
        super.onResume()
        loadPendingRequestsFromApi() // 화면으로 돌아올 때마다 새로고침
    }
}
