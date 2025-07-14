package com.accompany.purchaseManagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PurposeFragment : VoiceEnabledFragment() {

    private lateinit var etPurpose: EditText
    private lateinit var tvExamples: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_purpose_v2, container, false)

        etPurpose = view.findViewById(R.id.etPurpose)
        tvExamples = view.findViewById(R.id.tvExamples)

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

        return view
    }

    // 목적 가져오기
    fun getPurpose(): String = etPurpose.text.toString().trim()

    // 목적 유효성 검사
    fun isPurposeValid(): Boolean {
        return etPurpose.text.toString().trim().isNotEmpty()
    }
}
