package com.accompany.purchaseManagement.base

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// import com.accompany.purchaseManagement.BuildConfig
import com.accompany.purchaseManagement.PurchaseManagementApp
import com.accompany.purchaseManagement.utils.MemoryManager
import com.accompany.purchaseManagement.utils.PermissionManager
import kotlinx.coroutines.launch

/**
 * 모든 Activity의 공통 기능을 제공하는 베이스 클래스
 */
abstract class BaseActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "BaseActivity"
    }
    
    // 공통 유틸리티
    protected val memoryManager by lazy { MemoryManager.getInstance() }
    protected val permissionManager by lazy { PermissionManager() }
    
    // 로딩 상태 관리
    private var isLoading = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 메모리 관리자에 Activity 등록
        memoryManager.registerActivity(this)
        
        // 예외 처리 설정
        setupExceptionHandler()
        
        Log.d(TAG, "${this::class.simpleName} created")
    }
    
    override fun onResume() {
        super.onResume()
        
        // 메모리 사용량 로깅 (디버그 모드에서만)
        // if (BuildConfig.DEBUG) {
            memoryManager.logMemoryUsage(this, this::class.simpleName ?: TAG)
        // }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 로딩 상태 해제
        hideLoading()
        
        Log.d(TAG, "${this::class.simpleName} destroyed")
    }
    
    /**
     * 예외 처리기 설정
     */
    private fun setupExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in ${this::class.simpleName}", throwable)
            
            // 사용자에게 친화적인 오류 메시지 표시
            runOnUiThread {
                try {
                    showErrorMessage("앱에서 오류가 발생했습니다. 다시 시도해주세요.")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to show error message", e)
                }
            }
            
            // 기본 예외 처리기 호출
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * 로딩 상태 표시
     */
    protected fun showLoading() {
        if (isLoading) return
        
        isLoading = true
        
        try {
            // 로딩 UI가 있다면 표시
            findViewById<View?>(android.R.id.progress)?.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show loading", e)
        }
    }
    
    /**
     * 로딩 상태 숨김
     */
    protected fun hideLoading() {
        if (!isLoading) return
        
        isLoading = false
        
        try {
            // 로딩 UI가 있다면 숨김
            findViewById<View?>(android.R.id.progress)?.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide loading", e)
        }
    }
    
    /**
     * 안전한 Toast 메시지 표시
     */
    protected fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        try {
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, message, duration).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show toast", e)
        }
    }
    
    /**
     * 오류 메시지 표시
     */
    protected fun showErrorMessage(message: String) {
        showToast(message, Toast.LENGTH_LONG)
    }
    
    /**
     * 성공 메시지 표시
     */
    protected fun showSuccessMessage(message: String) {
        showToast(message, Toast.LENGTH_SHORT)
    }
    
    /**
     * 안전한 코루틴 실행
     */
    protected fun launchSafely(
        onError: (Throwable) -> Unit = { showErrorMessage("작업 중 오류가 발생했습니다") },
        block: suspend () -> Unit
    ) {
        lifecycleScope.launch {
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Error in coroutine", e)
                onError(e)
            }
        }
    }
    
    /**
     * 사용자 로그인 상태 확인
     */
    protected fun isUserLoggedIn(): Boolean {
        return try {
            val prefs = getSharedPreferences(PurchaseManagementApp.PREFS_NAME, MODE_PRIVATE)
            prefs.getBoolean(PurchaseManagementApp.KEY_IS_LOGGED_IN, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status", e)
            false
        }
    }
    
    /**
     * 현재 사용자 ID 가져오기
     */
    protected fun getCurrentUserId(): String? {
        return try {
            val prefs = getSharedPreferences(PurchaseManagementApp.PREFS_NAME, MODE_PRIVATE)
            prefs.getString(PurchaseManagementApp.KEY_USER_ID, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user ID", e)
            null
        }
    }
    
    /**
     * 현재 사용자 역할 가져오기
     */
    protected fun getCurrentUserRole(): String? {
        return try {
            val prefs = getSharedPreferences(PurchaseManagementApp.PREFS_NAME, MODE_PRIVATE)
            prefs.getString(PurchaseManagementApp.KEY_USER_ROLE, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user role", e)
            null
        }
    }
    
    /**
     * 권한 요청 결과 처리
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        permissionManager.handlePermissionResult(
            requestCode = requestCode,
            permissions = permissions,
            grantResults = grantResults,
            onGranted = {
                onPermissionGranted(requestCode)
            },
            onDenied = { deniedPermissions ->
                onPermissionDenied(requestCode, deniedPermissions)
            }
        )
    }
    
    /**
     * 권한 승인 시 호출되는 메서드 (하위 클래스에서 오버라이드)
     */
    protected open fun onPermissionGranted(requestCode: Int) {
        Log.d(TAG, "Permission granted: $requestCode")
    }
    
    /**
     * 권한 거부 시 호출되는 메서드 (하위 클래스에서 오버라이드)
     */
    protected open fun onPermissionDenied(requestCode: Int, deniedPermissions: List<String>) {
        Log.w(TAG, "Permission denied: $requestCode, $deniedPermissions")
        showErrorMessage("필요한 권한이 거부되었습니다")
    }
    
    /**
     * 뷰 안전 접근 헬퍼
     */
    protected fun <T : View> safelyFindViewById(id: Int): T? {
        return try {
            findViewById<T>(id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to find view with id: $id", e)
            null
        }
    }
    
    /**
     * 액션바 설정 헬퍼
     */
    protected fun setupActionBar(title: String, showBackButton: Boolean = false) {
        try {
            supportActionBar?.apply {
                setTitle(title)
                setDisplayHomeAsUpEnabled(showBackButton)
                setDisplayShowHomeEnabled(showBackButton)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup action bar", e)
        }
    }
    
    /**
     * 뒤로 가기 버튼 처리
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}