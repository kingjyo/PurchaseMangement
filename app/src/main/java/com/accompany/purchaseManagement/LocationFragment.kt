package com.accompany.purchaseManagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LocationFragment : VoiceEnabledFragment() {

    private lateinit var etLocation: EditText
    private lateinit var tvOptional: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_location_v2, container, false)

        etLocation = view.findViewById(R.id.etLocation)
        tvOptional = view.findViewById(R.id.tvOptional)

        tvOptional.text = "💡 선택사항이지만 입력해주시면 도움이 됩니다."

        // 음성 입력 설정
        val fabMic = view.findViewById<FloatingActionButton>(R.id.fabMic)
        setupVoiceInput(etLocation, fabMic) // 음성 입력 설정

        return view
    }

    // 장비명 가져오기
    fun getLocation(): String = etLocation.text.toString().trim()
}
