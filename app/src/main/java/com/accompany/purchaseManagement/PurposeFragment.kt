package com.accompany.purchaseManagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PurposeFragment : VoiceEnabledFragment() {

    private lateinit var etPurpose: EditText
    private lateinit var tvExamples: TextView
    private lateinit var tvHelp: TextView  // 유효성 검사 결과를 보여줄 TextView 추가

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_purpose_v2, container, false)

        etPurpose = view.findViewById(R.id.etPurpose)
        tvExamples = view.findViewById(R.id.tvExamples)
        tvHelp = view.findViewById(R.id.tvHelp)  // 유효성 검사 도움말 TextView

        // 예시 텍스트 설정
        tvExamples.text = """
            💡 예시:
            • 사료 배합 작업
            • 트랙터 수리
            • 축사 환경 개선
            • 장비 교체
        """.trimIndent()

        // 음성 입력 설정
        val fabMic = view.findViewById<FloatingActionButton>(R.id.fabMic)
        setupVoiceInput(etPurpose, fabMic) // 음성 입력 설정

        // TextWatcher로 실시간 유효성 검사
        etPurpose.addTextChangedListener {
            validatePurpose()
        }

        return view
    }

    // 목적 가져오기
    fun getPurpose(): String = etPurpose.text.toString().trim()

    // 목적 유효성 검사
    private fun validatePurpose() {
        val purpose = etPurpose.text.toString().trim()
        if (purpose.isEmpty()) {
            tvHelp.text = "💡 용도를 입력해주세요"
            tvHelp.setTextColor(resources.getColor(R.color.error_color))  // 빨간색으로 경고 메시지 표시
        } else {
            tvHelp.text = "✅ 용도가 올바르게 입력되었습니다."
            tvHelp.setTextColor(resources.getColor(R.color.success_color))  // 초록색으로 성공 메시지 표시
        }
    }

    // 목적 유효성 검사 결과를 반환
    fun isPurposeValid(): Boolean {
        return etPurpose.text.toString().trim().isNotEmpty()
    }
}

