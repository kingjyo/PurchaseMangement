package com.accompany.purchaseManagement.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import com.accompany.purchaseManagement.R
import com.accompany.purchaseManagement.PurchaseRequestActivityV2

class NoteFragment : BaseVoiceFragment() {
    
    private lateinit var etNote: EditText
    private lateinit var btnMic: ImageButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_v2, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View 초기화
        etNote = view.findViewById(R.id.etNote)
        btnMic = view.findViewById(R.id.btnMic)
        
        // 음성 입력 설정
        setupVoiceInput(etNote, btnMic)
        
        // 텍스트 변경 리스너
        etNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateParentActivity()
            }
        })
        
        // 기존 데이터 복원
        restoreData()
    }
    
    private fun updateParentActivity() {
        (activity as? PurchaseRequestActivityV2)?.let { parentActivity ->
            parentActivity.purchaseRequest = parentActivity.purchaseRequest.copy(
                note = etNote.text.toString().trim()
            )
        }
    }
    
    private fun restoreData() {
        (activity as? PurchaseRequestActivityV2)?.let { parentActivity ->
            val note = parentActivity.purchaseRequest.note
            if (note.isNotEmpty()) {
                etNote.setText(note)
            }
        }
    }
    
    fun validateInput(): Boolean {
        // 기타사항은 선택사항이므로 항상 true 반환
        return true
    }
}