package com.accompany.purchaseManagement

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PhotoFragment : Fragment() {

    private lateinit var btnAddPhoto: Button
    private lateinit var rvPhotos: RecyclerView
    private lateinit var tvPhotoCount: TextView
    private lateinit var tvOptional: TextView

    private lateinit var photoAdapter: SimplePhotoPreviewAdapter
    private val photoUris = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo, container, false)

        btnAddPhoto = view.findViewById(R.id.btnAddPhoto)
        rvPhotos = view.findViewById(R.id.rvPhotos)
        tvPhotoCount = view.findViewById(R.id.tvPhotoCount)
        tvOptional = view.findViewById(R.id.tvOptional)

        tvOptional.text = """
            📸 사진 첨부 (선택사항)
            
            • 고장 상황
            • 구매 제품 예시
            • 문제 상황 등
            
            여러 장 첨부 가능합니다.
        """.trimIndent()

        setupRecyclerView()
        setupClickListeners()
        updatePhotoCount()

        // Activity의 photoUris 가져오기
        (activity as? PurchaseRequestActivityV2)?.let { act ->
            // 이미 추가된 사진들이 있으면 표시
            val existingUris = act.photoUris
            if (existingUris.isNotEmpty()) {
                photoUris.clear()
                photoUris.addAll(existingUris)
                photoAdapter.notifyDataSetChanged()
                updatePhotoCount()
            }
        }

        return view
    }

    private fun setupRecyclerView() {
        photoAdapter = SimplePhotoPreviewAdapter(photoUris)
        rvPhotos.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = photoAdapter
        }
    }

    private fun setupClickListeners() {
        btnAddPhoto.setOnClickListener {
            showPhotoOptions()
        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("📷 사진 촬영", "🖼️ 갤러리에서 선택")

        AlertDialog.Builder(requireContext())
            .setTitle("사진 추가")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> (activity as? PurchaseRequestActivityV2)?.openCamera()
                    1 -> (activity as? PurchaseRequestActivityV2)?.openGallery()
                }
            }
            .show()
    }

    fun onPhotoAdded() {
        // Activity에서 사진이 추가되었을 때 호출
        (activity as? PurchaseRequestActivityV2)?.let { act ->
            photoUris.clear()
            photoUris.addAll(act.photoUris)
            photoAdapter.notifyDataSetChanged()
            updatePhotoCount()
        }
    }

    private fun updatePhotoCount() {
        tvPhotoCount.text = when (photoUris.size) {
            0 -> "사진이 없습니다"
            1 -> "1장의 사진이 첨부되었습니다"
            else -> "${photoUris.size}장의 사진이 첨부되었습니다"
        }
    }

    fun getPhotoUris(): List<Uri> = photoUris
}