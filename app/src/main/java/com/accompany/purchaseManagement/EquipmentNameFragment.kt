package com.accompany.purchaseManagement

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.activityViewModels

class EquipmentNameFragment : VoiceEnabledFragment() {

    private lateinit var etEquipmentName: EditText
    private lateinit var tvHelp: TextView

    private val viewModel: PurchaseViewModel by activityViewModels()  // 공유 ViewModel

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

        // 입력 도움말 및 유효성 검사
        etEquipmentName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                viewModel.equipmentName.value = input  // input 사용
                Log.d("EquipmentNameFragment", "equipmentName updated in ViewModel: $input")

                // 값에 따라 도움말 텍스트 변경
                when {
                    input.isEmpty() -> {
                        tvHelp.text = "💡 구매하실 장비나 물품의 이름을 입력하거나 음성으로 말씀해주세요"
                    }
                    input.length < 2 -> {
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

    // 장비명 가져오기 (ViewModel에서 반환)
    fun getEquipmentName(): String {
        return viewModel.equipmentName.value ?: etEquipmentName.text.toString().trim()
    }

    // 장비명 유효성 검사 (ViewModel 사용)
    fun isEquipmentNameValid(): Boolean {
        val name = viewModel.equipmentName.value ?: ""
        return name.isNotEmpty() && name.length >= 2
    }
}


