package com.accompany.purchaseManagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NoteFragment : VoiceEnabledFragment() {

    private lateinit var etNote: EditText
    private lateinit var tvOptional: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_v2, container, false)

        etNote = view.findViewById(R.id.etNote)
        tvOptional = view.findViewById(R.id.tvOptional)

        // 선택사항 안내 텍스트 설정
        tvOptional.text = """
            💡 선택사항입니다.
            
            추가로 전달하실 내용이 있으면 입력해주세요.
            예: 긴급 처리 요청, 특별 요구사항 등
        """.trimIndent()

        // 음성 입력 설정
        val fabMic = view.findViewById<FloatingActionButton>(R.id.fabMic)
        setupVoiceInput(etNote, fabMic) // 음성 입력 설정

        return view
    }

    // 노트 내용 가져오기
    fun getNote(): String = etNote.text.toString().trim()
}
