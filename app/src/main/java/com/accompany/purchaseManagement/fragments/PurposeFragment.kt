package com.accompany.purchaseManagement.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.accompany.purchaseManagement.R
import com.accompany.purchaseManagement.PurchaseRequestActivityV2
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PurposeFragment : BaseVoiceFragment() {
    
    private lateinit var etPurpose: EditText
    private lateinit var fabMic: FloatingActionButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_purpose_v2, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View 초기화
        etPurpose = view.findViewById(R.id.etPurpose)
        fabMic = view.findViewById(R.id.fabMic)
        
        // 음성 입력 설정
        setupVoiceInput(etPurpose, fabMic)
        
        // 텍스트 변경 리스너
        etPurpose.addTextChangedListener(object : TextWatcher {
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
                purpose = etPurpose.text.toString().trim()
            )
        }
    }
    
    private fun restoreData() {
        (activity as? PurchaseRequestActivityV2)?.let { parentActivity ->
            val purpose = parentActivity.purchaseRequest.purpose
            if (purpose.isNotEmpty()) {
                etPurpose.setText(purpose)
            }
        }
    }
    
    fun getPurpose(): String {
        return etPurpose.text.toString().trim()
    }
    
    fun isPurposeValid(): Boolean {
        return etPurpose.text.toString().trim().isNotEmpty()
    }
    
    fun validateInput(): Boolean {
        val purpose = etPurpose.text.toString().trim()
        return if (purpose.isEmpty()) {
            etPurpose.error = "용도를 입력해주세요"
            false
        } else {
            etPurpose.error = null
            true
        }
    }
}