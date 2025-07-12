package com.accompany.purchaseManagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

// 4. 용도 입력 Fragment
class PurposeFragment : Fragment() {

    private lateinit var etPurpose: EditText
    private lateinit var tvExamples: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_purpose, container, false)

        etPurpose = view.findViewById(R.id.etPurpose)
        tvExamples = view.findViewById(R.id.tvExamples)

        // 예시 표시
        tvExamples.text = """
            💡 예시:
            • 사료 배합 작업
            • 트랙터 수리
            • 축사 환경 개선
            • 장비 교체
        """.trimIndent()

        return view
    }

    fun getPurpose(): String = etPurpose.text.toString().trim()
}

// 5. 기타사항 입력 Fragment (선택사항)
class NoteFragment : Fragment() {

    private lateinit var etNote: EditText
    private lateinit var tvOptional: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note, container, false)

        etNote = view.findViewById(R.id.etNote)
        tvOptional = view.findViewById(R.id.tvOptional)

        tvOptional.text = """
            💡 선택사항입니다.
            
            추가로 전달하실 내용이 있으면 입력해주세요.
            예: 긴급 처리 요청, 특별 요구사항 등
        """.trimIndent()

        return view
    }

    fun getNote(): String = etNote.text.toString().trim()
}