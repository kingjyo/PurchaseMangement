package com.accompany.purchaseManagement

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.accompany.purchaseManagement.UserInfo

class PurchaseRequestActivityV2 : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }

    // ViewPager 관련
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar

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

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var dbHelper: PurchaseRequestDbHelper
    private lateinit var fcmHelper: FcmNotificationHelper
    private lateinit var emailHelper: EmailHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_request_v2)

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
            if (validateCurrentPage()) {
                if (viewPager.currentItem < 5) { // 총 6페이지
                    viewPager.currentItem = viewPager.currentItem + 1
                } else {
                    submitPurchaseRequest()
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
                0 -> "장비명"
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
                val fragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
                        as? EquipmentNameFragment
                equipmentName = fragment?.getEquipmentName() ?: ""
                if (equipmentName.isEmpty()) {
                    Toast.makeText(this, "장비명을 입력해주세요", Toast.LENGTH_SHORT).show()
                    false
                } else true
            }
            1 -> { // 수량
                val fragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
                        as? QuantityFragment
                quantity = fragment?.getQuantity() ?: "1"
                if (quantity.isEmpty() || quantity.toIntOrNull() == null || quantity.toInt() <= 0) {
                    Toast.makeText(this, "올바른 수량을 입력해주세요", Toast.LENGTH_SHORT).show()
                    false
                } else true
            }
            2 -> { // 장소 (선택)
                val fragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
                        as? LocationFragment
                location = fragment?.getLocation() ?: ""
                true
            }
            3 -> { // 용도
                val fragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
                        as? PurposeFragment
                purpose = fragment?.getPurpose() ?: ""
                if (purpose.isEmpty()) {
                    Toast.makeText(this, "사용 용도를 입력해주세요", Toast.LENGTH_SHORT).show()
                    false
                } else true
            }
            4 -> { // 기타사항 (선택)
                val fragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
                        as? NoteFragment
                note = fragment?.getNote() ?: ""
                true
            }
            5 -> { // 사진 (선택)
                val fragment = supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}")
                        as? PhotoFragment
                photoUris.clear()
                photoUris.addAll(fragment?.getPhotoUris() ?: emptyList())
                true
            }
            else -> true
        }
    }

    private fun submitPurchaseRequest() {
        // 사용자 정보 확인
        if (currentUser == null) {
            Toast.makeText(this, "로그인 정보를 확인할 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        // 최종 확인 다이얼로그
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
            .setPositiveButton("제출") { _, _ ->
                performSubmit()
            }
            .setNegativeButton("취소", null)
            .show()
    }

// PurchaseRequestActivityV2_Part1.kt에서 이어서...

    private fun performSubmit() {
        btnNext.isEnabled = false
        progressBar.visibility = View.VISIBLE

        val applicantName = currentUser?.name ?: "미설정"
        val applicantDepartment = currentUser?.department ?: "미설정"
        val applicantEmail = currentUser?.email ?: ""
        val requestDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date())

        lifecycleScope.launch {
            try {
                // 1. 사진 업로드 (있을 경우)
                val photoUrls = if (photoUris.isNotEmpty()) {
                    uploadPhotos(photoUris)
                } else {
                    emptyList()
                }

                // 2. Firestore에 저장
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

                // 3. 로컬 DB 저장 (백업)
                dbHelper.insertPurchaseRequest(
                    applicantName, applicantDepartment, equipmentName,
                    location, purpose, note, requestDate, PurchaseStatus.PENDING.displayName
                )

                // 4. Google Sheets 저장
                val googleSheetsHelper = GoogleSheetsHelper(this@PurchaseRequestActivityV2)
                val sheetsSuccess = googleSheetsHelper.submitToGoogleSheets(
                    applicantName, applicantDepartment, equipmentName,
                    location, purpose, note, requestDate,
                    hasPhoto = photoUrls.isNotEmpty(),
                    photoUrls = photoUrls.joinToString(",")
                )

                // 5. 이메일 전송
                emailHelper.sendPurchaseRequestEmail(
                    applicantName, applicantDepartment, equipmentName,
                    quantity, location, purpose, note, requestDate, photoUrls
                )

                // 6. 관리자에게 FCM 알림
                fcmHelper.notifyAdminNewRequest(
                    applicantName, equipmentName, requestId
                )

                // 성공 처리
                showSuccessDialog()

            } catch (e: Exception) {
                Toast.makeText(
                    this@PurchaseRequestActivityV2,
                    "제출 중 오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
            .setMessage("""
                구매신청이 성공적으로 제출되었습니다!
                
                📊 관리자가 실시간으로 확인할 수 있습니다
                📧 상세 내용이 이메일로 전송되었습니다
                
                구매신청 현황에서 진행상황을 확인하세요.
            """.trimIndent())
            .setPositiveButton("확인") { _, _ ->
                setResult(Activity.RESULT_OK)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // 카메라/갤러리 관련 메서드들
    fun openCamera() {
        val photoFile = createImageFile()
        val photoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        photoUris.add(photoUri)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "사진 선택"), REQUEST_IMAGE_PICK)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                // PhotoFragment에 전달
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

// 전체 코드를 합치려면:
// 1. PurchaseRequestActivityV2_Part1.kt의 내용을 복사
// 2. "// PurchaseRequestActivityV2_Part2.kt에서 계속..." 부분을 삭제
// 3. PurchaseRequestActivityV2_Part2.kt의 내용을 이어서 붙여넣기