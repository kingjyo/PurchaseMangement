package com.accompany.purchaseManagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etDepartment: EditText
    private lateinit var btnSave: Button
    private lateinit var googleAuthHelper: GoogleAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        etName = findViewById(R.id.etName)
        etDepartment = findViewById(R.id.etDepartment)
        btnSave = findViewById(R.id.btnSave)

        googleAuthHelper = GoogleAuthHelper(this)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val department = etDepartment.text.toString().trim()
            Log.d("ProfileSetup", "Save click: $name / $department")  // ① 버튼 클릭 시 값 확인

            if (name.isEmpty() || department.isEmpty()) {
                Toast.makeText(this, "이름과 소속을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                Log.d("ProfileSetup", "입력값 없음")                    // ② 유효성 체크
                return@setOnClickListener
            }

            val currentUser = googleAuthHelper.getCurrentUser()
            Log.d("ProfileSetup", "currentUser: $currentUser")         // ③ 현재 유저 정보 확인

            if (currentUser == null) {
                Toast.makeText(this, "로그인 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
                Log.d("ProfileSetup", "currentUser == null")           // ④ 예외 처리
                startActivity(Intent(this, LoginActivityV2::class.java))
                finish()
                return@setOnClickListener
            }

            googleAuthHelper.updateUserInfo(
                email = currentUser.email,
                name = name,
                department = department
            ) { success ->
                Log.d("ProfileSetup", "updateUserInfo callback: $success")  // ⑤ 콜백 결과 확인
                if (success) {
                    Toast.makeText(this, "프로필이 저장되었습니다!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    Log.d("ProfileSetup", "MainActivity로 이동!")
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "저장에 실패했습니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                    Log.d("ProfileSetup", "저장 실패")
                }
            }
        }
    }}

