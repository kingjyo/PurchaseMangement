package com.accompany.purchaseManagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.accompany.purchaseManagement.UserInfo

class PurchaseStatusActivityV2 : AppCompatActivity() {

    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var lvRequests: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var llEmptyState: View
    private lateinit var fabRefresh: FloatingActionButton

    private lateinit var requestAdapter: PurchaseRequestListAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var googleAuthHelper: GoogleAuthHelper
    private lateinit var fcmHelper: FcmNotificationHelper

    private var currentUser: UserInfo? = null
    private var selectedStatus: String = "전체"
    private val requestList = mutableListOf<PurchaseRequestV2>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_status_improved)

        supportActionBar?.title = "구매신청 현황"

        googleAuthHelper = GoogleAuthHelper(this)
        fcmHelper = FcmNotificationHelper(this)
        currentUser = googleAuthHelper.getCurrentUser()

        initViews()
        setupChips()
        setupListView()
        loadRequests()
    }

    private fun initViews() {
        chipGroupStatus = findViewById(R.id.chipGroupStatus)
        lvRequests = findViewById(R.id.lvRequests)
        progressBar = findViewById(R.id.progressBar)
        llEmptyState = findViewById(R.id.llEmptyState)
        fabRefresh = findViewById(R.id.fabRefresh)

        // 관리자만 새로고침 버튼 표시
        fabRefresh.visibility = if (currentUser?.isAdmin == true) View.VISIBLE else View.GONE
        fabRefresh.setOnClickListener {
            loadRequests()
        }
    }

    private fun setupChips() {
        chipGroupStatus.setOnCheckedChangeListener { _, checkedId ->
            selectedStatus = when (checkedId) {
                R.id.chipAll -> "전체"
                R.id.chipPending -> PurchaseStatus.PENDING.displayName
                R.id.chipConfirmed -> PurchaseStatus.CONFIRMED.displayName
                R.id.chipInApproval -> PurchaseStatus.IN_APPROVAL.displayName
                R.id.chipApproved -> PurchaseStatus.APPROVED.displayName
                R.id.chipPreProcessed -> PurchaseStatus.PRE_PROCESSED.displayName
                R.id.chipCompleted -> PurchaseStatus.COMPLETED.displayName
                else -> "전체"
            }
            loadRequests()
        }
    }

    private fun setupListView() {
        requestAdapter = PurchaseRequestListAdapter(
            context = this,
            requests = requestList,
            currentUser = currentUser,
            onItemClick = { request ->
                if (currentUser?.isAdmin == true) {
                    showStatusChangeDialog(request)
                } else if (request.applicantEmail == currentUser?.email && request.isModifiable()) {
                    showModifyDialog(request)
                }
            },
            onEditClick = { request ->
                if (request.applicantEmail == currentUser?.email && request.isModifiable()) {
                    openEditActivity(request)
                }
            }
        )

        lvRequests.adapter = requestAdapter
    }

    private fun loadRequests() {
        progressBar.visibility = View.VISIBLE
        llEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                var query: Query = db.collection("purchaseRequests")
                    .orderBy("requestDate", Query.Direction.DESCENDING)

                // 상태 필터링
                if (selectedStatus != "전체") {
                    query = query.whereEqualTo("status", selectedStatus)
                }

                val snapshot = query.get().await()

                requestList.clear()
                for (doc in snapshot.documents) {
                    val request = PurchaseRequestV2.fromFirebaseDocument(
                        doc.id,
                        doc.data ?: emptyMap()
                    )
                    requestList.add(request)
                }

                requestAdapter.notifyDataSetChanged()

                if (requestList.isEmpty()) {
                    llEmptyState.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                Toast.makeText(this@PurchaseStatusActivityV2,
                    "데이터 로드 실패: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showStatusChangeDialog(request: PurchaseRequestV2) {
        val currentStatus = PurchaseStatus.fromString(request.status)
        val nextStatuses = PurchaseStatus.getAllAdminStatuses()

        val statusNames = nextStatuses.map { "${it.emoji} ${it.displayName}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("상태 변경")
            .setItems(statusNames) { _, which ->
                val newStatus = nextStatuses[which]
                updateRequestStatus(request, newStatus)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateRequestStatus(request: PurchaseRequestV2, newStatus: PurchaseStatus) {
        lifecycleScope.launch {
            try {
                val updates = hashMapOf<String, Any>(
                    "status" to newStatus.displayName,
                    "processor" to (currentUser?.name ?: "관리자"),
                    "processedDate" to java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        java.util.Locale.KOREA
                    ).format(java.util.Date())
                )

                db.collection("purchaseRequests")
                    .document(request.requestId)
                    .update(updates)
                    .await()

                // 신청자에게 알림
                fcmHelper.notifyRequesterStatusChanged(
                    request.applicantEmail,
                    request.equipmentName,
                    request.status,
                    newStatus.displayName,
                    request.requestId
                )

                Toast.makeText(this@PurchaseStatusActivityV2,
                    "상태가 변경되었습니다",
                    Toast.LENGTH_SHORT).show()

                loadRequests()

            } catch (e: Exception) {
                Toast.makeText(this@PurchaseStatusActivityV2,
                    "상태 변경 실패: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showModifyDialog(request: PurchaseRequestV2) {
        AlertDialog.Builder(this)
            .setTitle("구매신청 수정")
            .setMessage("이 구매신청을 수정하시겠습니까?")
            .setPositiveButton("수정") { _, _ -> openEditActivity(request) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun openEditActivity(request: PurchaseRequestV2) {
        val intent = Intent(this, EditPurchaseRequestActivity::class.java).apply {
            putExtra(EditPurchaseRequestActivity.EXTRA_REQUEST_ID, request.requestId)
            putExtra(EditPurchaseRequestActivity.EXTRA_REQUEST_DATA, request)
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadRequests()
        }
    }

    override fun onResume() {
        super.onResume()
        loadRequests()
    }
}
