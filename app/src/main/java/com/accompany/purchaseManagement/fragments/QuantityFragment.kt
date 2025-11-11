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

class QuantityFragment : BaseVoiceFragment() {
    
    private lateinit var etQuantity: EditText
    private lateinit var btnMic: ImageButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quantity, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View 초기화
        etQuantity = view.findViewById(R.id.etQuantity)
        btnMic = view.findViewById(R.id.btnMic)
        
        // 음성 입력 설정
        setupVoiceInput(etQuantity, btnMic)
        
        // 텍스트 변경 리스너
        etQuantity.addTextChangedListener(object : TextWatcher {
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
                quantity = etQuantity.text.toString().trim()
            )
        }
    }
    
    private fun restoreData() {
        (activity as? PurchaseRequestActivityV2)?.let { parentActivity ->
            val quantity = parentActivity.purchaseRequest.quantity
            if (quantity.isNotEmpty()) {
                etQuantity.setText(quantity)
            }
        }
    }
    
    fun validateInput(): Boolean {
        val quantity = etQuantity.text.toString().trim()
        return if (quantity.isEmpty()) {
            etQuantity.error = "수량을 입력해주세요"
            false
        } else {
            etQuantity.error = null
            true
        }
    }
}