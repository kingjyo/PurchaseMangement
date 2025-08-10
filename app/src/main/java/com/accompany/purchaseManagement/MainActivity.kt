package com.accompany.purchaseManagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.accompany.purchaseManagement.data.models.User
import com.accompany.purchaseManagement.utils.PermissionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // UI 요소들
    private lateinit var tvWelcome: TextView
    private lateinit var btnPurchaseRequest: Button
    private lateinit var btnPurchaseStatus: Button
    private lateinit var btnPurchaseHistory: Button
    private lateinit var btnCattleStatus: Button
    private lateinit var btnAdmin: Button
    
    // 현재 사용자 정보
    private var currentUser: User? = null
    
    // 권한 관리자
    private val permissionManager = PermissionManager()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 툴바 설정
        supportActionBar?.apply {
            title = "구매신청 관리"
            setDisplayShowTitleEnabled(true)
        }
        
        // 로그인 상태 확인
        if (!checkLoginStatus()) {
            navigateToLogin()
            return
        }
        
        // UI 초기화
        initializeViews()
        loadUserInfo()
        setupButtons()
        requestNecessaryPermissions()
    }
    
    private fun checkLoginStatus(): Boolean {
        val prefs = getSharedPreferences(PurchaseManagementApp.PREFS_NAME, MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean(PurchaseManagementApp.KEY_IS_LOGGED_IN, false)
        
        Log.d(TAG, "Login status: $isLoggedIn")
        return isLoggedIn
    }
    
    private fun navigateToLogin() {
        Log.d(TAG, "Navigating to login")
        val intent = Intent(this, LoginActivity2::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun initializeViews() {
        try {
            tvWelcome = findViewById(R.id.tvWelcome)
            btnPurchaseRequest = findViewById(R.id.btnPurchaseRequest)
            btnPurchaseStatus = findViewById(R.id.btnPurchaseStatus)
            btnPurchaseHistory = findViewById(R.id.btnPurchaseHistory)
            btnCattleStatus = findViewById(R.id.btnCattleStatus)
            btnAdmin = findViewById(R.id.btnAdmin)
            
            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize views", e)
            Toast.makeText(this, "화면을 불러오는 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun loadUserInfo() {
        lifecycleScope.launch {
            try {
                val prefs = getSharedPreferences(PurchaseManagementApp.PREFS_NAME, MODE_PRIVATE)
                val userName = prefs.getString(PurchaseManagementApp.KEY_USER_NAME, "") ?: ""
                val userEmail = prefs.getString(PurchaseManagementApp.KEY_USER_EMAIL, "") ?: ""
                val userDepartment = prefs.getString(PurchaseManagementApp.KEY_USER_DEPARTMENT, "") ?: ""
                val userRole = prefs.getString(PurchaseManagementApp.KEY_USER_ROLE, User.ROLE_USER) ?: User.ROLE_USER
                val userId = prefs.getString(PurchaseManagementApp.KEY_USER_ID, "") ?: ""
                
                currentUser = User(
                    id = userId,
                    name = userName,
                    email = userEmail,
                    department = userDepartment,
                    role = userRole
                )
                
                updateUI()
                Log.d(TAG, "User info loaded: $userName ($userRole)")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load user info", e)
                Toast.makeText(this@MainActivity, "사용자 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
        }
    }
    
    private fun updateUI() {
        currentUser?.let { user ->
            // 환영 메시지 업데이트
            val welcomeMessage = "${user.name}님 (${user.department})\n${user.getDisplayRole()}"
            tvWelcome.text = welcomeMessage
            
            // 역할에 따른 버튼 표시/숨김
            updateButtonVisibility(user)
        }
    }
    
    private fun updateButtonVisibility(user: User) {
        // 일반 기능 버튼들은 모든 사용자에게 표시
        btnPurchaseRequest.visibility = Button.VISIBLE
        btnPurchaseStatus.visibility = Button.VISIBLE
        btnPurchaseHistory.visibility = Button.VISIBLE
        btnCattleStatus.visibility = Button.VISIBLE
        
        // 관리자 버튼은 매니저 이상에게만 표시
        btnAdmin.visibility = if (user.isManager()) Button.VISIBLE else Button.GONE
        
        Log.d(TAG, "UI updated for user role: ${user.role}")
    }
    
    private fun setupButtons() {
        btnPurchaseRequest.setOnClickListener {
            Log.d(TAG, "Purchase request button clicked")
            navigateToActivity(PurchaseRequestActivityV2::class.java)
        }
        
        btnPurchaseStatus.setOnClickListener {
            Log.d(TAG, "Purchase status button clicked")
            navigateToActivity(PurchaseStatusActivityV2::class.java)
        }
        
        btnPurchaseHistory.setOnClickListener {
            Log.d(TAG, "Purchase history button clicked")
            navigateToActivity(PurchaseHistoryActivity::class.java)
        }
        
        btnCattleStatus.setOnClickListener {
            Log.d(TAG, "Cattle status button clicked")
            navigateToActivity(CattleStatusActivity::class.java)
        }
        
        btnAdmin.setOnClickListener {
            Log.d(TAG, "Admin button clicked")
            if (currentUser?.isManager() == true) {
                navigateToActivity(UserManagementActivity::class.java)
            } else {
                Toast.makeText(this, "관리자 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToActivity(activityClass: Class<*>) {
        try {
            val intent = Intent(this, activityClass)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to navigate to ${activityClass.simpleName}", e)
            Toast.makeText(this, "화면을 열 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun requestNecessaryPermissions() {
        // 앱에서 필요한 기본 권한들을 요청
        lifecycleScope.launch {
            try {
                // 알림 권한 요청 (Android 13+)
                if (!permissionManager.hasNotificationPermission(this@MainActivity)) {
                    permissionManager.requestNotificationPermissions(this@MainActivity)
                }
                
                Log.d(TAG, "Permission check completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking permissions", e)
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.action_refresh -> {
                refreshData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃하시겠습니까?")
            .setPositiveButton("로그아웃") { _, _ ->
                performLogout()
            }
            .setNegativeButton("취소", null)
            .show()
    }
    
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // 사용자 세션 정리
                PurchaseManagementApp.getInstance().clearUserSession()
                
                Log.d(TAG, "User logged out successfully")
                Toast.makeText(this@MainActivity, "로그아웃되었습니다", Toast.LENGTH_SHORT).show()
                
                // 로그인 화면으로 이동
                navigateToLogin()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
                Toast.makeText(this@MainActivity, "로그아웃 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun refreshData() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Refreshing data...")
                loadUserInfo()
                Toast.makeText(this@MainActivity, "데이터를 새로고침했습니다", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
                Toast.makeText(this@MainActivity, "새로고침 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
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
                Log.d(TAG, "Permissions granted for request code: $requestCode")
            },
            onDenied = { deniedPermissions ->
                Log.w(TAG, "Permissions denied: $deniedPermissions")
                
                when (requestCode) {
                    PermissionManager.REQUEST_NOTIFICATION_PERMISSION -> {
                        // 알림 권한은 선택사항이므로 별도 처리하지 않음
                    }
                }
            }
        )
    }
    
    override fun onResume() {
        super.onResume()
        
        // 로그인 상태 재확인
        if (!checkLoginStatus()) {
            navigateToLogin()
            return
        }
        
        // UI 업데이트
        currentUser?.let { updateUI() }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity destroyed")
    }
}