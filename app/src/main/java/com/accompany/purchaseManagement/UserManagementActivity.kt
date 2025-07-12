package com.accompany.purchaseManagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserManagementActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyMessage: TextView
    private lateinit var btnAddUser: Button

    private lateinit var userAdapter: UserAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userList = mutableListOf<UserData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        supportActionBar?.title = "사용자 관리"

        initViews()
        setupRecyclerView()
        loadUsers()
    }

    private fun initViews() {
        rvUsers = findViewById(R.id.rvUsers)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        btnAddUser = findViewById(R.id.btnAddUser)

        btnAddUser.setOnClickListener {
            Toast.makeText(this,
                "새 사용자는 앱에 처음 로그인할 때 자동으로 추가됩니다",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(userList) { user ->
            showEditUserDialog(user)
        }

        rvUsers.apply {
            layoutManager = LinearLayoutManager(this@UserManagementActivity)
            adapter = userAdapter
        }
    }

    private fun loadUsers() {
        progressBar.visibility = View.VISIBLE
        tvEmptyMessage.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val snapshot = db.collection("users")
                    .orderBy("name")
                    .get()
                    .await()

                userList.clear()
                for (doc in snapshot.documents) {
                    val user = UserData(
                        email = doc.id,
                        name = doc.getString("name") ?: "미설정",
                        department = doc.getString("department") ?: "미설정",
                        isAdmin = doc.getBoolean("isAdmin") ?: false,
                        fcmToken = doc.getString("fcmToken") ?: ""
                    )
                    userList.add(user)
                }

                userAdapter.notifyDataSetChanged()

                if (userList.isEmpty()) {
                    tvEmptyMessage.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                Toast.makeText(this@UserManagementActivity,
                    "사용자 목록 로드 실패: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showEditUserDialog(user: UserData) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user, null)
        val etName = dialogView.findViewById<EditText>(R.id.etUserName)
        val etDepartment = dialogView.findViewById<EditText>(R.id.etUserDepartment)
        val cbIsAdmin = dialogView.findViewById<CheckBox>(R.id.cbIsAdmin)

        etName.setText(user.name)
        etDepartment.setText(user.department)
        cbIsAdmin.isChecked = user.isAdmin

        // 자기 자신은 관리자 권한 변경 불가
        if (user.email == AppConfig.MANAGER_EMAIL) {
            cbIsAdmin.isEnabled = false
        }

        AlertDialog.Builder(this)
            .setTitle("사용자 정보 수정")
            .setMessage("이메일: ${user.email}")
            .setView(dialogView)
            .setPositiveButton("저장") { _, _ ->
                val newName = etName.text.toString().trim()
                val newDepartment = etDepartment.text.toString().trim()
                val newIsAdmin = cbIsAdmin.isChecked

                if (newName.isEmpty() || newDepartment.isEmpty()) {
                    Toast.makeText(this, "이름과 소속은 필수입니다", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updateUser(user.email, newName, newDepartment, newIsAdmin)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateUser(email: String, name: String, department: String, isAdmin: Boolean) {
        lifecycleScope.launch {
            try {
                val updates = hashMapOf<String, Any>(
                    "name" to name,
                    "department" to department,
                    "isAdmin" to isAdmin,
                    "updatedAt" to System.currentTimeMillis()
                )

                db.collection("users").document(email)
                    .update(updates)
                    .await()

                Toast.makeText(this@UserManagementActivity,
                    "사용자 정보가 업데이트되었습니다",
                    Toast.LENGTH_SHORT).show()

                loadUsers() // 목록 새로고침

            } catch (e: Exception) {
                Toast.makeText(this@UserManagementActivity,
                    "업데이트 실패: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 사용자 데이터 클래스
    data class UserData(
        val email: String,
        val name: String,
        val department: String,
        val isAdmin: Boolean,
        val fcmToken: String
    )

    // RecyclerView 어댑터
    inner class UserAdapter(
        private val users: List<UserData>,
        private val onItemClick: (UserData) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(users[position])
        }

        override fun getItemCount() = users.size

        inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
            private val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
            private val tvUserDepartment: TextView = itemView.findViewById(R.id.tvUserDepartment)
            private val ivAdminBadge: ImageView = itemView.findViewById(R.id.ivAdminBadge)
            private val ivTokenStatus: ImageView = itemView.findViewById(R.id.ivTokenStatus)

            fun bind(user: UserData) {
                tvUserName.text = user.name
                tvUserEmail.text = user.email
                tvUserDepartment.text = user.department

                // 관리자 뱃지
                ivAdminBadge.visibility = if (user.isAdmin) View.VISIBLE else View.GONE

                // FCM 토큰 상태
                ivTokenStatus.setImageResource(
                    if (user.fcmToken.isNotEmpty())
                        R.drawable.ic_notifications_active
                    else
                        R.drawable.ic_notifications_off
                )

                itemView.setOnClickListener {
                    onItemClick(user)
                }
            }
        }
    }
}