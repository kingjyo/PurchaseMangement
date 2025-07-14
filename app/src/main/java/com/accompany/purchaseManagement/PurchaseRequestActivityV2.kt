package com.accompany.purchaseManagement

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PurchaseRequestActivityV2 : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
        private const val REQUEST_CODE_PERMISSIONS = 100
    }


    // ViewPager 관련
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar
    private var currentPhotoUri: Uri? = null

    // 사용자 정보
    private lateinit var googleAuthHelper: GoogleAuthHelper
    private var currentUser: UserInfo? = null

    // 입력 데이터
    private var equipmentName = ""
    private var quantity = "1"
    private var location = ""
    private var purpose = ""
    private var note = ""
    var photoUris = mutableListOf<Uri>()
    private val viewModel: PurchaseViewModel by viewModels()
    // 사진 촬영을 위한 ActivityResultLauncher
    private val photoCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // 이미 저장된 currentPhotoUri 사용
                currentPhotoUri?.let { uri ->
                    photoUris.add(uri)
                    // PhotoFragment 업데이트
                    val fragment = supportFragmentManager
                        .findFragmentByTag("f${viewPager.currentItem}") as? PhotoFragment
                    fragment?.onPhotoAdded()
                }
            }
            currentPhotoUri = null // 사용 후 초기화
        }

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var dbHelper: PurchaseRequestDbHelper
    private lateinit var fcmHelper: FcmNotificationHelper
    private lateinit var emailHelper: EmailHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_request_v2)

        viewPager = findViewById(R.id.viewPager)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)

        // 슬라이드로 페이지 전환 비활성화
        viewPager.isUserInputEnabled = false

        btnPrevious.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }

        supportActionBar?.title = "구매신청"

        // 초기화
        googleAuthHelper = GoogleAuthHelper(this)
        currentUser = googleAuthHelper.getCurrentUser()
        dbHelper = PurchaseRequestDbHelper(this)
        fcmHelper = FcmNotificationHelper(this)
        emailHelper = EmailHelper(this)

        initViews()
        setupViewPager()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        progressBar = findViewById(R.id.progressBar)

        btnPrevious.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }

        btnNext.setOnClickListener {
            // 유효성 검사 수행
            if (validateCurrentPage()) {
                if (viewPager.currentItem < 5) { // 총 6페이지
                    viewPager.currentItem = viewPager.currentItem + 1
                } else {
                    submitPurchaseRequest()  // 최종 제출
                }
            }
        }
    }

    private fun setupViewPager() {
        val adapter = PurchaseRequestPagerAdapter(this)
        viewPager.adapter = adapter

        // 탭 설정
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "장비명/품목"
                1 -> "수량"
                2 -> "장소"
                3 -> "용도"
                4 -> "기타"
                5 -> "사진"
                else -> ""
            }
        }.attach()

        // 페이지 변경 리스너
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButtons(position)
            }
        })
    }

    private fun updateButtons(position: Int) {
        // 이전 버튼
        btnPrevious.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE

        // 다음 버튼 텍스트
        btnNext.text = when (position) {
            5 -> "제출"
            2, 4, 5 -> "다음 (선택사항)"
            else -> "다음"
        }
    }

    private fun validateCurrentPage(): Boolean {
        return when (viewPager.currentItem) {
            0 -> { // 장비명
                val fragment = supportFragmentManager.findFragmentByTag("f0") as? EquipmentNameFragment
                fragment?.isEquipmentNameValid() ?: false
            }
            1 -> { // 수량
                if (!viewModel.isQuantityValid()) {
                    Toast.makeText(this, "올바른 수량을 입력해주세요", Toast.LENGTH_SHORT).show()
                    false
                } else true
            }
            2 -> true  // 장소 (선택사항)
            3 -> { // 용도
                val fragment = supportFragmentManager.findFragmentByTag("f3") as? PurposeFragment
                fragment?.isPurposeValid() ?: false
            }
            4 -> true  // 기타사항 (선택사항)
            5 -> true  // 사진 (선택사항)
            else -> true
        }
    }





    // 구매신청 제출
    private fun submitPurchaseRequest() {
        if (currentUser == null) {
            Toast.makeText(this, "로그인 정보를 확인할 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }
        showSubmitConfirmDialog()
    }

    private fun showSubmitConfirmDialog() {
        val message = """
            📋 구매신청 내용 확인
            👤 신청자: ${currentUser?.name} (${currentUser?.department})
            🔧 장비명: $equipmentName
            🔢 수량: $quantity
            ${if (location.isNotEmpty()) "📍 장소: $location\n" else ""}
            📝 용도: $purpose
            ${if (note.isNotEmpty()) "💬 기타: $note\n" else ""}
            📸 사진: ${photoUris.size}장
            
            위 내용으로 구매신청을 제출하시겠습니까?
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("구매신청 확인")
            .setMessage(message)
            .setPositiveButton("제출") { _, _ -> performSubmit() }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun performSubmit() {
        btnNext.isEnabled = false
        progressBar.visibility = View.VISIBLE
        val applicantName = currentUser?.name ?: "미설정"
        val applicantDepartment = currentUser?.department ?: "미설정"
        val applicantEmail = currentUser?.email ?: ""
        val requestDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())

        equipmentName = viewModel.equipmentName.value ?: ""
        quantity = viewModel.quantity.value ?: "1"
        location = viewModel.location.value ?: ""
        purpose = viewModel.purpose.value ?: ""
        note = viewModel.note.value ?: ""

        lifecycleScope.launch {
            try {
                // 사진 업로드
                val photoUrls = if (photoUris.isNotEmpty()) uploadPhotos(photoUris) else emptyList()

                // Firestore에 저장
                val requestData = hashMapOf(
                    "applicantName" to applicantName,
                    "applicantDepartment" to applicantDepartment,
                    "applicantEmail" to applicantEmail,
                    "equipmentName" to equipmentName,
                    "quantity" to quantity,
                    "location" to location,
                    "purpose" to purpose,
                    "note" to note,
                    "photoUrls" to photoUrls,
                    "requestDate" to requestDate,
                    "status" to PurchaseStatus.PENDING.displayName,
                    "modifyCount" to 0
                )

                val docRef = db.collection("purchaseRequests")
                    .add(requestData)
                    .await()

                val requestId = docRef.id

                // 로컬 DB 저장
                dbHelper.insertPurchaseRequest(applicantName, applicantDepartment, equipmentName, location, purpose, note, requestDate, PurchaseStatus.PENDING.displayName)

                // Google Sheets 저장
                val googleSheetsHelper = GoogleSheetsHelper(this@PurchaseRequestActivityV2)
                val sheetsSuccess = googleSheetsHelper.submitToGoogleSheets(applicantName, applicantDepartment, equipmentName, location, purpose, note, requestDate, hasPhoto = photoUrls.isNotEmpty(), photoUrls = photoUrls.joinToString(","))

                // 이메일 전송
                emailHelper.sendPurchaseRequestEmail(applicantName, applicantDepartment, equipmentName, quantity, location, purpose, note, requestDate, photoUrls)

                // 관리자에게 FCM 알림
                fcmHelper.notifyAdminNewRequest(applicantName, equipmentName, requestId)

                // 성공 처리
                showSuccessDialog()

            } catch (e: Exception) {
                Toast.makeText(this@PurchaseRequestActivityV2, "제출 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                btnNext.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun uploadPhotos(uris: List<Uri>): List<String> {
        val urls = mutableListOf<String>()
        for (uri in uris) {
            val filename = "purchase_photos/${System.currentTimeMillis()}_${(0..9999).random()}.jpg"
            val ref = storage.reference.child(filename)
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            urls.add(url)
        }
        return urls
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("✅ 구매신청 완료")
            .setMessage("""구매신청이 성공적으로 제출되었습니다!""")
            .setPositiveButton("확인") { _, _ ->
                setResult(Activity.RESULT_OK)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // 카메라 권한을 확인하고, 권한이 없으면 권한을 요청합니다.
    fun openCamera() {
        // 카메라 권한이 없으면 권한을 요청
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS)
        } else {
            // 권한이 이미 있으면 카메라 실행
            val photoFile = createImageFile()
            currentPhotoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            currentPhotoUri?.let { uri ->
                photoCaptureLauncher.launch(uri)
            }
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되었으면 카메라 실행
                openCamera()
            } else {
                // 권한이 거부되었을 때 처리
                Toast.makeText(this, "카메라 권한이 거부되었습니다. 권한을 허용해야 카메라를 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 사진을 저장할 파일 생성
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }


    fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "사진 선택"), REQUEST_IMAGE_PICK)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                val fragment = supportFragmentManager.findFragmentByTag("f5") as? PhotoFragment
                fragment?.onPhotoAdded()
            }
            REQUEST_IMAGE_PICK -> {
                data?.let {
                    if (it.clipData != null) {
                        for (i in 0 until it.clipData!!.itemCount) {
                            photoUris.add(it.clipData!!.getItemAt(i).uri)
                        }
                    } else {
                        it.data?.let { uri -> photoUris.add(uri) }
                    }

                    val fragment = supportFragmentManager.findFragmentByTag("f5") as? PhotoFragment
                    fragment?.onPhotoAdded()
                }
            }
        }
    }
}
