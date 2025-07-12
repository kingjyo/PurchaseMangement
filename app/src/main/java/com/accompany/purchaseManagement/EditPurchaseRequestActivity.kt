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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditPurchaseRequestActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_REQUEST_ID = "request_id"
        const val EXTRA_REQUEST_DATA = "request_data"
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }

    private lateinit var etEquipmentName: EditText
    private lateinit var etQuantity: EditText
    private lateinit var etLocation: EditText
    private lateinit var etPurpose: EditText
    private lateinit var etNote: EditText
    private lateinit var btnAddPhoto: Button
    private lateinit var rvPhotos: RecyclerView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var photoAdapter: PhotoAdapter
    private val photoUris = mutableListOf<Uri>()
    private val existingPhotoUrls = mutableListOf<String>()
    private val deletedPhotoUrls = mutableListOf<String>()

    private lateinit var requestId: String
    private lateinit var originalRequest: PurchaseRequestV2
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var fcmHelper: FcmNotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_purchase_request)

        supportActionBar?.title = "구매신청 수정"

        // 전달받은 데이터
        requestId = intent.getStringExtra(EXTRA_REQUEST_ID) ?: ""
        originalRequest = intent.getSerializableExtra(EXTRA_REQUEST_DATA) as? PurchaseRequestV2
            ?: run {
                Toast.makeText(this, "잘못된 요청입니다", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        fcmHelper = FcmNotificationHelper(this)

        initViews()
        setupPhotoRecyclerView()
        loadOriginalData()
    }

    private fun initViews() {
        etEquipmentName = findViewById(R.id.etEquipmentName)
        etQuantity = findViewById(R.id.etQuantity)
        etLocation = findViewById(R.id.etLocation)
        etPurpose = findViewById(R.id.etPurpose)
        etNote = findViewById(R.id.etNote)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        rvPhotos = findViewById(R.id.rvPhotos)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)

        btnAddPhoto.setOnClickListener {
            showPhotoOptions()
        }

        btnSave.setOnClickListener {
            saveChanges()
        }

        btnCancel.setOnClickListener {
            showCancelConfirmDialog()
        }
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoAdapter(
            existingUrls = existingPhotoUrls,
            newUris = photoUris,
            onDeleteExisting = { url ->
                existingPhotoUrls.remove(url)
                deletedPhotoUrls.add(url)
                photoAdapter.notifyDataSetChanged()
            },
            onDeleteNew = { uri ->
                photoUris.remove(uri)
                photoAdapter.notifyDataSetChanged()
            }
        )

        rvPhotos.apply {
            layoutManager = GridLayoutManager(this@EditPurchaseRequestActivity, 3)
            adapter = photoAdapter
        }
    }

    private fun loadOriginalData() {
        etEquipmentName.setText(originalRequest.equipmentName)
        etQuantity.setText(originalRequest.quantity)
        etLocation.setText(originalRequest.location)
        etPurpose.setText(originalRequest.purpose)
        etNote.setText(originalRequest.note)

        // 기존 사진 로드
        existingPhotoUrls.addAll(originalRequest.photoUrls)
        photoAdapter.notifyDataSetChanged()
    }

    private fun showPhotoOptions() {
        val options = arrayOf("사진 촬영", "갤러리에서 선택")
        AlertDialog.Builder(this)
            .setTitle("사진 추가")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
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

    private fun openGallery() {
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
                photoAdapter.notifyDataSetChanged()
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
                    photoAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun saveChanges() {
        val equipmentName = etEquipmentName.text.toString().trim()
        val quantity = etQuantity.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val purpose = etPurpose.text.toString().trim()
        val note = etNote.text.toString().trim()

        // 유효성 검사
        if (equipmentName.isEmpty() || quantity.isEmpty() || purpose.isEmpty()) {
            Toast.makeText(this, "필수 항목을 모두 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 변경사항 확인
        val modifiedFields = mutableListOf<String>()
        if (equipmentName != originalRequest.equipmentName) modifiedFields.add("장비명")
        if (quantity != originalRequest.quantity) modifiedFields.add("수량")
        if (location != originalRequest.location) modifiedFields.add("장소")
        if (purpose != originalRequest.purpose) modifiedFields.add("용도")
        if (note != originalRequest.note) modifiedFields.add("기타사항")
        if (photoUris.isNotEmpty() || deletedPhotoUrls.isNotEmpty()) modifiedFields.add("사진")

        if (modifiedFields.isEmpty()) {
            Toast.makeText(this, "변경된 내용이 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        // 저장 확인 다이얼로그
        AlertDialog.Builder(this)
            .setTitle("수정 확인")
            .setMessage("다음 항목이 수정됩니다:\n${modifiedFields.joinToString(", ")}\n\n저장하시겠습니까?")
            .setPositiveButton("저장") { _, _ ->
                performSave(equipmentName, quantity, location, purpose, note, modifiedFields)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun performSave(
        equipmentName: String,
        quantity: String,
        location: String,
        purpose: String,
        note: String,
        modifiedFields: List<String>
    ) {
        btnSave.isEnabled = false
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // 1. 새 사진 업로드
                val newPhotoUrls = if (photoUris.isNotEmpty()) {
                    uploadPhotos(photoUris)
                } else {
                    emptyList()
                }

                // 2. 최종 사진 URL 리스트
                val finalPhotoUrls = existingPhotoUrls + newPhotoUrls

                // 3. Firestore 업데이트
                val updates = hashMapOf<String, Any>(
                    "equipmentName" to equipmentName,
                    "quantity" to quantity,
                    "location" to location,
                    "purpose" to purpose,
                    "note" to note,
                    "photoUrls" to finalPhotoUrls,
                    "modifiedDate" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date()),
                    "modifyCount" to (originalRequest.modifyCount + 1)
                )

                db.collection("purchaseRequests")
                    .document(requestId)
                    .update(updates)
                    .await()

                // 4. Google Sheets 업데이트
                updateGoogleSheets(equipmentName, quantity, location, purpose, note, finalPhotoUrls)

                // 5. 관리자에게 알림
                fcmHelper.notifyAdminRequestModified(
                    originalRequest.applicantName,
                    equipmentName,
                    modifiedFields,
                    requestId
                )

                // 6. 삭제된 사진 정리 (Storage)
                deletedPhotoUrls.forEach { url ->
                    try {
                        storage.getReferenceFromUrl(url).delete().await()
                    } catch (e: Exception) {
                        // 삭제 실패는 무시
                    }
                }

                Toast.makeText(this@EditPurchaseRequestActivity,
                    "수정이 완료되었습니다",
                    Toast.LENGTH_SHORT).show()

                setResult(Activity.RESULT_OK)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@EditPurchaseRequestActivity,
                    "수정 실패: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            } finally {
                btnSave.isEnabled = true
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

    private suspend fun updateGoogleSheets(
        equipmentName: String,
        quantity: String,
        location: String,
        purpose: String,
        note: String,
        photoUrls: List<String>
    ) {
        // Google Sheets 업데이트 로직
        // 기존 GoogleSheetsHelper를 수정하거나 새로운 메서드 추가 필요
    }

    private fun showCancelConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("수정 취소")
            .setMessage("변경사항이 저장되지 않습니다. 정말 취소하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                finish()
            }
            .setNegativeButton("아니오", null)
            .show()
    }
}