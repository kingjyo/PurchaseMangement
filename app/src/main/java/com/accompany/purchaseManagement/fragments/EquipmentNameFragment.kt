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

class EquipmentNameFragment : BaseVoiceFragment() {
    
    private lateinit var etEquipmentName: EditText
    private lateinit var btnMic: ImageButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_equipment_name_v2, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View 초기화
        etEquipmentName = view.findViewById(R.id.etEquipmentName)
        btnMic = view.findViewById(R.id.btnMic)
        
        // 음성 입력 설정
        setupVoiceInput(etEquipmentName, btnMic)
        
        // 텍스트 변경 리스너
        etEquipmentName.addTextChangedListener(object : TextWatcher {
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
                equipmentName = etEquipmentName.text.toString().trim()
            )
        }
    }
    
    private fun restoreData() {
        (activity as? PurchaseRequestActivityV2)?.let { parentActivity ->
            val equipmentName = parentActivity.purchaseRequest.equipmentName
            if (equipmentName.isNotEmpty()) {
                etEquipmentName.setText(equipmentName)
            }
        }
    }
    
    fun validateInput(): Boolean {
        val equipmentName = etEquipmentName.text.toString().trim()
        return if (equipmentName.isEmpty()) {
            etEquipmentName.error = "장비/품목명을 입력해주세요"
            false
        } else {
            etEquipmentName.error = null
            true
        }
    }
}