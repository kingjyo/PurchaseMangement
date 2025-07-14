package com.accompany.purchaseManagement

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EquipmentNameFragment : VoiceEnabledFragment() {

    private lateinit var etEquipmentName: EditText
    private lateinit var tvHelp: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_equipment_name_v2, container, false)

        etEquipmentName = view.findViewById(R.id.etEquipmentName)
        tvHelp = view.findViewById(R.id.tvHelp)

        // 음성 입력 설정
        val fabMic = view.findViewById<FloatingActionButton>(R.id.fabMic)
        setupVoiceInput(etEquipmentName, fabMic) // 음성 입력 설정

        // 포커스 자동 설정
        etEquipmentName.requestFocus()

        // 입력 도움말
        etEquipmentName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    s.isNullOrEmpty() -> {
                        tvHelp.text = "💡 구매하실 장비나 물품의 이름을 입력하거나 음성으로 말씀해주세요"
                    }
                    s.length < 2 -> {
                        tvHelp.text = "💡 좀 더 자세히 입력해주세요"
                    }
                    else -> {
                        tvHelp.text = "✅ 좋습니다!"
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    // 장비명 가져오기
    fun getEquipmentName(): String = etEquipmentName.text.toString().trim()

    // 장비명 유효성 검사
    fun isEquipmentNameValid(): Boolean {
        val equipmentName = etEquipmentName.text.toString().trim()
        return equipmentName.isNotEmpty()
    }
}
