package com.accompany.purchaseManagement.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * 권한 관리를 위한 유틸리티 클래스
 */
class PermissionManager {
    
    companion object {
        private const val TAG = "PermissionManager"
        
        // 권한 요청 코드
        const val REQUEST_CAMERA_PERMISSION = 100
        const val REQUEST_STORAGE_PERMISSION = 101
        const val REQUEST_AUDIO_PERMISSION = 102
        const val REQUEST_NOTIFICATION_PERMISSION = 103
        const val REQUEST_MULTIPLE_PERMISSIONS = 104
        
        // 필요한 권한들
        val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
        
        val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        
        val AUDIO_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
        
        val NOTIFICATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            emptyArray()
        }
        
        val ALL_PERMISSIONS = CAMERA_PERMISSIONS + STORAGE_PERMISSIONS + AUDIO_PERMISSIONS + NOTIFICATION_PERMISSIONS
    }
    
    /**
     * 단일 권한 확인
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 여러 권한 확인
     */
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }
    
    /**
     * 카메라 권한 확인
     */
    fun hasCameraPermission(context: Context): Boolean {
        return hasPermissions(context, CAMERA_PERMISSIONS)
    }
    
    /**
     * 저장소 권한 확인
     */
    fun hasStoragePermission(context: Context): Boolean {
        return hasPermissions(context, STORAGE_PERMISSIONS)
    }
    
    /**
     * 오디오 권한 확인
     */
    fun hasAudioPermission(context: Context): Boolean {
        return hasPermissions(context, AUDIO_PERMISSIONS)
    }
    
    /**
     * 알림 권한 확인
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (NOTIFICATION_PERMISSIONS.isEmpty()) {
            true // Android 13 미만에서는 항상 true
        } else {
            hasPermissions(context, NOTIFICATION_PERMISSIONS)
        }
    }
    
    /**
     * Activity에서 단일 권한 요청
     */
    fun requestPermission(
        activity: Activity,
        permission: String,
        requestCode: Int,
        rationale: String? = null
    ) {
        if (hasPermission(activity, permission)) {
            Log.d(TAG, "Permission already granted: $permission")
            return
        }
        
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) && rationale != null) {
            showPermissionRationale(activity, rationale) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
            }
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        }
    }
    
    /**
     * Activity에서 여러 권한 요청
     */
    fun requestPermissions(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int,
        rationale: String? = null
    ) {
        val deniedPermissions = permissions.filter { !hasPermission(activity, it) }.toTypedArray()
        
        if (deniedPermissions.isEmpty()) {
            Log.d(TAG, "All permissions already granted")
            return
        }
        
        val shouldShowRationale = deniedPermissions.any { 
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it) 
        }
        
        if (shouldShowRationale && rationale != null) {
            showPermissionRationale(activity, rationale) {
                ActivityCompat.requestPermissions(activity, deniedPermissions, requestCode)
            }
        } else {
            ActivityCompat.requestPermissions(activity, deniedPermissions, requestCode)
        }
    }
    
    /**
     * Fragment에서 단일 권한 요청
     */
    fun requestPermission(
        fragment: Fragment,
        permission: String,
        requestCode: Int,
        rationale: String? = null
    ) {
        val context = fragment.requireContext()
        
        if (hasPermission(context, permission)) {
            Log.d(TAG, "Permission already granted: $permission")
            return
        }
        
        if (fragment.shouldShowRequestPermissionRationale(permission) && rationale != null) {
            showPermissionRationale(context, rationale) {
                fragment.requestPermissions(arrayOf(permission), requestCode)
            }
        } else {
            fragment.requestPermissions(arrayOf(permission), requestCode)
        }
    }
    
    /**
     * Fragment에서 여러 권한 요청
     */
    fun requestPermissions(
        fragment: Fragment,
        permissions: Array<String>,
        requestCode: Int,
        rationale: String? = null
    ) {
        val context = fragment.requireContext()
        val deniedPermissions = permissions.filter { !hasPermission(context, it) }.toTypedArray()
        
        if (deniedPermissions.isEmpty()) {
            Log.d(TAG, "All permissions already granted")
            return
        }
        
        val shouldShowRationale = deniedPermissions.any { 
            fragment.shouldShowRequestPermissionRationale(it) 
        }
        
        if (shouldShowRationale && rationale != null) {
            showPermissionRationale(context, rationale) {
                fragment.requestPermissions(deniedPermissions, requestCode)
            }
        } else {
            fragment.requestPermissions(deniedPermissions, requestCode)
        }
    }
    
    /**
     * 특정 기능을 위한 권한 요청 (Activity)
     */
    fun requestCameraPermissions(activity: Activity) {
        requestPermissions(
            activity,
            CAMERA_PERMISSIONS,
            REQUEST_CAMERA_PERMISSION,
            "사진을 촬영하려면 카메라 권한이 필요합니다."
        )
    }
    
    fun requestStoragePermissions(activity: Activity) {
        requestPermissions(
            activity,
            STORAGE_PERMISSIONS,
            REQUEST_STORAGE_PERMISSION,
            "사진을 저장하고 불러오려면 저장소 권한이 필요합니다."
        )
    }
    
    fun requestAudioPermissions(activity: Activity) {
        requestPermissions(
            activity,
            AUDIO_PERMISSIONS,
            REQUEST_AUDIO_PERMISSION,
            "음성 입력 기능을 사용하려면 마이크 권한이 필요합니다."
        )
    }
    
    fun requestNotificationPermissions(activity: Activity) {
        if (NOTIFICATION_PERMISSIONS.isNotEmpty()) {
            requestPermissions(
                activity,
                NOTIFICATION_PERMISSIONS,
                REQUEST_NOTIFICATION_PERMISSION,
                "앱의 알림을 받으려면 알림 권한이 필요합니다."
            )
        }
    }
    
    /**
     * 특정 기능을 위한 권한 요청 (Fragment)
     */
    fun requestCameraPermissions(fragment: Fragment) {
        requestPermissions(
            fragment,
            CAMERA_PERMISSIONS,
            REQUEST_CAMERA_PERMISSION,
            "사진을 촬영하려면 카메라 권한이 필요합니다."
        )
    }
    
    fun requestStoragePermissions(fragment: Fragment) {
        requestPermissions(
            fragment,
            STORAGE_PERMISSIONS,
            REQUEST_STORAGE_PERMISSION,
            "사진을 저장하고 불러오려면 저장소 권한이 필요합니다."
        )
    }
    
    fun requestAudioPermissions(fragment: Fragment) {
        requestPermissions(
            fragment,
            AUDIO_PERMISSIONS,
            REQUEST_AUDIO_PERMISSION,
            "음성 입력 기능을 사용하려면 마이크 권한이 필요합니다."
        )
    }
    
    /**
     * 권한 요청 결과 처리
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: (deniedPermissions: List<String>) -> Unit,
        onNeverAskAgain: (neverAskPermissions: List<String>) -> Unit = { }
    ) {
        if (permissions.isEmpty() || grantResults.isEmpty()) {
            Log.w(TAG, "Empty permissions or grantResults")
            return
        }
        
        val deniedPermissions = mutableListOf<String>()
        val neverAskPermissions = mutableListOf<String>()
        
        permissions.forEachIndexed { index, permission ->
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission)
            }
        }
        
        if (deniedPermissions.isEmpty()) {
            Log.d(TAG, "All permissions granted for request code: $requestCode")
            onGranted()
        } else {
            Log.w(TAG, "Permissions denied for request code: $requestCode - $deniedPermissions")
            onDenied(deniedPermissions)
            
            // Never ask again 체크는 Activity나 Fragment에서 수행
            onNeverAskAgain(neverAskPermissions)
        }
    }
    
    /**
     * 권한 설명 다이얼로그 표시
     */
    private fun showPermissionRationale(context: Context, rationale: String, onProceed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("권한 필요")
            .setMessage(rationale)
            .setPositiveButton("허용") { _, _ -> onProceed() }
            .setNegativeButton("거부", null)
            .show()
    }
    
    /**
     * 앱 설정으로 이동
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app settings", e)
            
            // 대안으로 일반 앱 설정 화면으로 이동
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to open general app settings", e2)
            }
        }
    }
    
    /**
     * 권한 거부 시 설정으로 이동할지 묻는 다이얼로그
     */
    fun showPermissionDeniedDialog(
        context: Context,
        message: String = "이 기능을 사용하려면 권한이 필요합니다. 설정에서 권한을 허용해주세요."
    ) {
        AlertDialog.Builder(context)
            .setTitle("권한 필요")
            .setMessage(message)
            .setPositiveButton("설정으로 이동") { _, _ -> openAppSettings(context) }
            .setNegativeButton("취소", null)
            .show()
    }
}