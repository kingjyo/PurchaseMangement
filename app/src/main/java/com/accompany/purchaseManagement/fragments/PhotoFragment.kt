package com.accompany.purchaseManagement.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.accompany.purchaseManagement.adapters.PhotoAdapter
import com.accompany.purchaseManagement.PurchaseRequestActivityV2
import com.accompany.purchaseManagement.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoFragment : Fragment() {
    
    companion object {
        private const val TAG = "PhotoFragment"
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_STORAGE_PERMISSION = 101
        private const val MAX_PHOTOS = 5
    }
    
    private lateinit var tvPhotoCount: TextView
    private lateinit var rvPhotos: RecyclerView
    
    private lateinit var photoAdapter: PhotoAdapter
    private val photoUris = mutableListOf<Uri>()
    private var currentPhotoPath: String? = null
    
    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                val photoUri = Uri.fromFile(File(path))
                addPhoto(photoUri)
            }
        }
    }
    
    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { addPhoto(it) }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupButtons()
        restoreData()
    }
    
    private fun initViews(view: View) {
        val btnAddPhoto = view.findViewById<Button>(R.id.btnAddPhoto)
        tvPhotoCount = view.findViewById(R.id.tvPhotoCount)
        rvPhotos = view.findViewById(R.id.rvPhotos)
        
        btnAddPhoto.setOnClickListener {
            showPhotoSourceDialog()
        }
    }
    
    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter(
            existingUrls = emptyList(),
            newUris = photoUris,
            onRemove = { position ->
                removePhoto(position)
            }
        )
        
        rvPhotos.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = photoAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupButtons() {
        // Button is set up in initViews
    }
    
    private fun showPhotoSourceDialog() {
        if (photoUris.size >= MAX_PHOTOS) {
            Toast.makeText(context, "최대 ${MAX_PHOTOS}장까지 추가할 수 있습니다", Toast.LENGTH_SHORT).show()
            return
        }
        
        val options = arrayOf("카메라로 촬영", "갤러리에서 선택")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("사진 추가")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (checkCameraPermission()) {
                            openCamera()
                        } else {
                            requestCameraPermission()
                        }
                    }
                    1 -> {
                        if (checkStoragePermission()) {
                            openGallery()
                        } else {
                            requestStoragePermission()
                        }
                    }
                }
            }
            .show()
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }
    
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(permission),
            REQUEST_STORAGE_PERMISSION
        )
    }
    
    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }
            
            cameraLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera", e)
            Toast.makeText(context, "카메라를 열 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
    
    private fun openGallery() {
        try {
            galleryLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery", e)
            Toast.makeText(context, "갤러리를 열 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun addPhoto(uri: Uri) {
        if (photoUris.size >= MAX_PHOTOS) {
            Toast.makeText(context, "최대 ${MAX_PHOTOS}장까지 추가할 수 있습니다", Toast.LENGTH_SHORT).show()
            return
        }
        
        photoUris.add(uri)
        photoAdapter.notifyItemInserted(photoUris.size - 1)
        updatePhotoCount()
        updateParentActivity()
    }
    
    private fun removePhoto(position: Int) {
        if (position in photoUris.indices) {
            photoUris.removeAt(position)
            photoAdapter.notifyItemRemoved(position)
            photoAdapter.notifyItemRangeChanged(position, photoUris.size - position)
            updatePhotoCount()
            updateParentActivity()
        }
    }
    
    private fun updatePhotoCount() {
        tvPhotoCount.text = "사진 ${photoUris.size}/${MAX_PHOTOS}장"
    }
    
    private fun updateParentActivity() {
        (activity as? PurchaseRequestActivityV2)?.let { parentActivity ->
            parentActivity.localPhotoUris = ArrayList(photoUris)
        }
    }
    
    private fun restoreData() {
        (activity as? PurchaseRequestActivityV2)?.let { parentActivity ->
            photoUris.clear()
            photoUris.addAll(parentActivity.localPhotoUris)
            photoAdapter.notifyDataSetChanged()
            updatePhotoCount()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(context, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(context, "저장소 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    fun validateInput(): Boolean {
        // 사진은 선택사항이므로 항상 true 반환
        return true
    }
    
    fun getPhotoUris(): List<Uri> = photoUris.toList()
    
    // Called from parent activity when photo is added via camera
    fun onPhotoAdded() {
        photoAdapter.notifyDataSetChanged()
        updatePhotoCount()
    }
}