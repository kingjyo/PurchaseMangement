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

// 1. 장비명 입력 Fragment
class EquipmentNameFragment : Fragment() {

    private lateinit var etEquipmentName: EditText
    private lateinit var tvHelp: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_equipment_name, container, false)

        etEquipmentName = view.findViewById(R.id.etEquipmentName)
        tvHelp = view.findViewById(R.id.tvHelp)

        // 포커스 자동 설정
        etEquipmentName.requestFocus()

        // 입력 도움말
        etEquipmentName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    s.isNullOrEmpty() -> {
                        tvHelp.text = "💡 구매하실 장비나 물품의 이름을 입력해주세요"
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

    fun getEquipmentName(): String = etEquipmentName.text.toString().trim()
}

// 2. 수량 입력 Fragment
class QuantityFragment : Fragment() {

    private lateinit var etQuantity: EditText
    private lateinit var tvUnit: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quantity, container, false)

        etQuantity = view.findViewById(R.id.etQuantity)
        tvUnit = view.findViewById(R.id.tvUnit)

        // 기본값 설정
        etQuantity.setText("1")
        etQuantity.setSelection(etQuantity.text.length)

        // 포커스 및 키보드
        etQuantity.requestFocus()

        return view
    }

    fun getQuantity(): String = etQuantity.text.toString().trim()
}

// 3. 장소 입력 Fragment (선택사항)
class LocationFragment : Fragment() {

    private lateinit var etLocation: EditText
    private lateinit var tvOptional: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location, container, false)

        etLocation = view.findViewById(R.id.etLocation)
        tvOptional = view.findViewById(R.id.tvOptional)

        tvOptional.text = "💡 선택사항입니다. 필요하신 경우만 입력해주세요."

        return view
    }

    fun getLocation(): String = etLocation.text.toString().trim()
}