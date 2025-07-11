package com.accompany.purchaseManagement

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import com.accompany.purchaseManagement.FirestoreHelper


class PurchaseRequestActivity : AppCompatActivity() {

    private lateinit var etApplicantName: EditText
    private lateinit var etApplicantDepartment: EditText
    private lateinit var etEquipmentName: EditText
    private lateinit var etLocation: EditText
    private lateinit var etPurpose: EditText
    private lateinit var etNote: EditText
    private lateinit var ivPhoto: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var btnSubmit: Button
    private lateinit var tvRequestDate: TextView
    private val selectedPhotoUris = mutableListOf<Uri>()
    private suspend fun uploadPhotosToFirebase(uris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
        val storage = FirebaseStorage.getInstance()
        val urls = mutableListOf<String>()
        for (uri in uris) {
            val filename = "purchase_photos/${System.currentTimeMillis()}_${(0..9999).random()}.jpg"
            val ref = storage.reference.child(filename)
            ref.putFile(uri).await() // kotlinx-coroutines-play-services 필요
            val url = ref.downloadUrl.await().toString()
            urls.add(url)
        }
        urls
    }

    private var tempCameraUri: Uri? = null
    private lateinit var dbHelper: PurchaseRequestDbHelper

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_request)

        initViews()
        setupDateDisplay()
        setupClickListeners()

        dbHelper = PurchaseRequestDbHelper(this)
    }

    private fun initViews() {
        etApplicantName = findViewById(R.id.etApplicantName)
        etApplicantDepartment = findViewById(R.id.etApplicantDepartment)
        etEquipmentName = findViewById(R.id.etEquipmentName)
        etLocation = findViewById(R.id.etLocation)
        etPurpose = findViewById(R.id.etPurpose)
        etNote = findViewById(R.id.etNote)
        ivPhoto = findViewById(R.id.ivPhoto)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvRequestDate = findViewById(R.id.tvRequestDate)
    }

    private fun setupDateDisplay() {
        val currentDate = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(Date())
        tvRequestDate.text = "구매신청일: $currentDate"
    }

    private fun setupClickListeners() {
        btnTakePhoto.setOnClickListener {
            val options = arrayOf("사진 촬영", "갤러리에서 선택")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("사진 첨부 방식 선택")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpenCamera()
                    1 -> openGallery()
                }
            }
            builder.show()
        }

        btnSubmit.setOnClickListener {
            submitPurchaseRequest()
        }
    }

    // 카메라 권한 체크 후 촬영
    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        tempCameraUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri)
        }
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }


    // 카메라 촬영
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    // 갤러리에서 선택
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "사진 선택"), REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                tempCameraUri?.let { selectedPhotoUris.add(it) }
            }
            REQUEST_IMAGE_PICK -> {
                data?.let {
                    val clipData = it.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            selectedPhotoUris.add(clipData.getItemAt(i).uri)
                        }
                    } else {
                        it.data?.let { uri -> selectedPhotoUris.add(uri) }
                    }
                }
            }
        }
        // 한 장만 미리보기
        if (selectedPhotoUris.isNotEmpty()) {
            ivPhoto.setImageURI(selectedPhotoUris[0])
        }






































































































































        
    }

    // ... (생략: createImageFile, onRequestPermissionsResult 등)

    private fun submitPurchaseRequest() {
        val applicantName = etApplicantName.text.toString().trim()
        val applicantDepartment = etApplicantDepartment.text.toString().trim()
        val equipmentName = etEquipmentName.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val purpose = etPurpose.text.toString().trim()
        val note = etNote.text.toString().trim()
        val requestDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())

        if (applicantName.isEmpty() || applicantDepartment.isEmpty() ||
            equipmentName.isEmpty() || location.isEmpty() || purpose.isEmpty()) {
            Toast.makeText(this, "필수 정보를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "전송 중..."

        lifecycleScope.launch {
            try {
                // 1. 로컬 DB 저장 (오프라인 대응)
                val localSuccess = dbHelper.insertPurchaseRequest(
                    applicantName, applicantDepartment, equipmentName,
                    location, purpose, note, requestDate, "대기중"
                )

                // 2. 사진 업로드 (Storage)
                val photoUrls = uploadPhotosToFirebase(selectedPhotoUris) // List<String>

                // 3. Google Sheets 저장 (원래 있던 방식)
                val googleSheetsHelper = GoogleSheetsHelper(this@PurchaseRequestActivity)
                val sheetsSuccess = googleSheetsHelper.submitToGoogleSheets(
                    applicantName, applicantDepartment, equipmentName,
                    location, purpose, note, requestDate,
                    hasPhoto = photoUrls.isNotEmpty(),
                    photoUrls = photoUrls.joinToString(",")
                )

                // 4. 이메일 전송 (필요 시)
                if (photoUrls.isNotEmpty()) {
                    sendPhotosByEmail(
                        applicantName, applicantDepartment, equipmentName,
                        location, purpose, requestDate, selectedPhotoUris
                    )
                }

                // 5. **Firestore 저장 (최신 추가!)**
                val firestoreData = mapOf(
                    "applicantName" to applicantName,
                    "applicantDepartment" to applicantDepartment,
                    "equipmentName" to equipmentName,
                    "location" to location,
                    "purpose" to purpose,
                    "note" to note,
                    "requestDate" to requestDate,
                    "photoUrls" to photoUrls,
                    "status" to "대기중"
                )
                FirestoreHelper.savePurchaseRequest(firestoreData)

                // ===> 성공/실패 분기 메시지 처리
                when {
                    sheetsSuccess -> {
                        val message = if (photoUrls.isNotEmpty()) {
                            "✅ 구매신청이 완료되었습니다!\n📊 신청내역: Google Sheets에 실시간 저장\n📸 사진: 이메일로 전송됨"
                        } else {
                            "✅ 구매신청이 완료되었습니다!\n관리자가 실시간으로 확인할 수 있습니다."
                        }
                        Toast.makeText(this@PurchaseRequestActivity, message, Toast.LENGTH_LONG).show()
                        clearInputFields()
                        btnSubmit.postDelayed({ finish() }, 2000)
                    }
                    localSuccess -> {
                        Toast.makeText(
                            this@PurchaseRequestActivity,
                            "⚠️ 구매신청은 저장되었지만\n인터넷 연결을 확인해주세요.\n나중에 다시 시도하거나 관리자에게 알려주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                        clearInputFields()
                        finish()
                    }
                    else -> {
                        Toast.makeText(
                            this@PurchaseRequestActivity,
                            "❌ 저장 중 오류가 발생했습니다.\n다시 시도해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this@PurchaseRequestActivity, "오류 발생: 인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show()
            } finally {
                btnSubmit.isEnabled = true
                btnSubmit.text = "구매신청 제출"
            }
        }
    }


    // ✅ 여러 장 이메일 첨부 (photoBitmap, photoFile 완전 제거)
    private fun sendPhotosByEmail(
        applicantName: String,
        applicantDepartment: String,
        equipmentName: String,
        location: String,
        purpose: String,
        requestDate: String,
        photoUris: List<Uri>
    ) {
        try {
            val managerEmail = AppConfig.MANAGER_EMAIL
            val subject = "[구매신청-사진] $applicantName - $equipmentName"
            val emailBody = """
                📸 구매신청 첨부사진
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                👤 신청자: $applicantName ($applicantDepartment)
                🔧 장비/물품: $equipmentName
                📍 사용 장소: $location
                📝 사용 용도: $purpose
                📅 신청일시: $requestDate
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                💡 본 이메일은 구매신청 사진을 별도로 전송하는 것입니다.
                📊 신청 내역은 Google Sheets에서 확인하세요.
                
                ※ 자동 생성된 이메일입니다.
            """.trimIndent()

            // 여러 장 첨부
            val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(managerEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
                putParcelableArrayListExtra(
                    Intent.EXTRA_STREAM,
                    ArrayList(photoUris)
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(emailIntent, "사진을 이메일로 전송"))

        } catch (e: Exception) {
            Toast.makeText(this, "이메일 전송 준비 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun clearInputFields() {
        etApplicantName.text.clear()
        etApplicantDepartment.text.clear()
        etEquipmentName.text.clear()
        etLocation.text.clear()
        etPurpose.text.clear()
        etNote.text.clear()
        ivPhoto.setImageDrawable(null)

        // 포커스를 첫 번째 입력창으로
        etApplicantName.requestFocus()

    // ... (clearInputFields 등 기존 메서드 유지)
}



    }
