package com.accompany.purchaseManagement

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class PurchaseHistoryActivity : AppCompatActivity() {

    private lateinit var lvAllRequests: ListView
    private lateinit var btnExportToExcel: Button
    private lateinit var dbHelper: PurchaseRequestDbHelper
    private lateinit var adapter: PurchaseRequestListAdapter
    private var allRequests: List<PurchaseRequest> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_history)

        supportActionBar?.title = "구매신청 기록"

        dbHelper = PurchaseRequestDbHelper(this)

        initViews()
        loadAllRequests()
        setupClickListeners()
    }

    private fun initViews() {
        lvAllRequests = findViewById(R.id.lvAllRequests)
        btnExportToExcel = findViewById(R.id.btnExportToExcel)
    }

    fun loadAllRequests() {
        val allRequests = dbHelper.getAllRequests()  // PurchaseRequest로 반환된다고 가정

        // allRequests를 PurchaseRequestV2로 변환
        val allRequestsV2 = allRequests.map { request ->
            PurchaseRequestV2(
                requestId = request.requestId,
                applicantName = request.applicantName,
                applicantDepartment = request.applicantDepartment,
                applicantEmail = request.applicantEmail,
                equipmentName = request.equipmentName,
                quantity = request.quantity,
                location = request.location,
                purpose = request.purpose,
                note = request.note,
                photoUrls = request.photoUrls,
                requestDate = request.requestDate,
                status = request.status,
                modifiedDate = request.modifiedDate,
                modifyCount = request.modifyCount,
                processor = request.processor,
                processedDate = request.processedDate,
                processNote = request.processNote
            )
        }

        // PurchaseRequestV2 객체로 변환된 리스트를 adapter에 설정
        adapter = PurchaseRequestListAdapter(
            context = this,
            requests = allRequestsV2.toMutableList(),  // V2로 타입 맞춰서 전달
            currentUser = null,
            onItemClick = null,
            onEditClick = null
        )
        lvAllRequests.adapter = adapter
    }

    private fun setupClickListeners() {
        btnExportToExcel.setOnClickListener {
            if (allRequests.isEmpty()) {
                Toast.makeText(this, "추출할 데이터가 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            exportToCSV()
        }
    }

    private fun exportToCSV() {
        try {
            val fileName = "로컬구매신청기록_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())}.csv"
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            val writer = FileWriter(file)

            // CSV 헤더 (BOM 추가로 한글 깨짐 방지)
            writer.append("\uFEFF") // UTF-8 BOM
            writer.append("신청자명,소속,장비명,장소,용도,기타사항,신청일시,상태\n")

            // 데이터 추가
            allRequests.forEach { request ->
                writer.append("\"${request.applicantName}\",")
                writer.append("\"${request.applicantDepartment}\",")
                writer.append("\"${request.equipmentName}\",")
                writer.append("\"${request.location}\",")
                writer.append("\"${request.purpose}\",")
                writer.append("\"${request.note}\",")
                writer.append("\"${request.requestDate}\",")
                writer.append("\"${request.status}\"\n")
            }

            writer.close()

            // 파일 공유
            shareFile(file)

            Toast.makeText(this, "로컬 기록 엑셀 파일이 생성되었습니다 (${allRequests.size}개)", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "파일 생성 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFile(file: File) {
        val fileUri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "구매신청 로컬 기록")
            putExtra(Intent.EXTRA_TEXT, """
                구매신청 로컬 기록 파일입니다.
                
                📊 총 ${allRequests.size}개의 기록
                📅 추출일시: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())}
                
                ⚠️ 이 파일은 로컬 저장된 백업 데이터입니다.
                📊 실시간 데이터는 Google Sheets에서 확인하세요.
            """.trimIndent())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "로컬 기록 파일 공유"))
    }

    override fun onResume() {
        super.onResume()
        loadAllRequests() // 화면으로 돌아올 때마다 새로고침
    }
}