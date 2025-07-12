package com.accompany.purchaseManagement

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

// 1. 음성 입력이 가능한 기본 Fragment
abstract class VoiceEnabledFragment : Fragment() {

    protected lateinit var speechHelper: SpeechRecognitionHelper
    protected var micButton: FloatingActionButton? = null
    protected var isVoiceMode = false

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            speechHelper = SpeechRecognitionHelper(it)
        }
    }

    protected fun setupVoiceInput(editText: EditText, micButton: FloatingActionButton) {
        this.micButton = micButton

        // 음성 인식 콜백 설정
        speechHelper.setCallback(object : SpeechRecognitionHelper.SpeechRecognitionCallback {
            override fun onResults(text: String) {
                activity?.runOnUiThread {
                    val currentText = editText.text.toString()
                    val newText = if (currentText.isEmpty()) text else "$currentText $text"
                    editText.setText(newText)
                    editText.setSelection(editText.text.length)
                }
            }

            override fun onError(error: String) {
                activity?.runOnUiThread {
                    updateMicButtonState(false)
                }
            }

            override fun onReadyForSpeech() {
                activity?.runOnUiThread {
                    micButton.setImageResource(R.drawable.ic_mic_active)
                }
            }

            override fun onEndOfSpeech() {
                activity?.runOnUiThread {
                    micButton.setImageResource(R.drawable.ic_mic)
                }
            }

            override fun onPartialResults(partialText: String) {
                // 실시간 표시 (선택사항)
            }
        })

        // 마이크 버튼 클릭 리스너
        micButton.setOnClickListener {
            if (checkAudioPermission()) {
                toggleVoiceInput()
            } else {
                requestAudioPermission()
            }
        }
    }

    private fun toggleVoiceInput() {
        isVoiceMode = !isVoiceMode
        if (isVoiceMode) {
            speechHelper.toggleContinuousRecognition()
            updateMicButtonState(true)
        } else {
            speechHelper.stopListening()
            updateMicButtonState(false)
        }
    }

    private fun updateMicButtonState(isActive: Boolean) {
        micButton?.apply {
            if (isActive) {
                setImageResource(R.drawable.ic_mic_active)
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.error_color)
            } else {
                setImageResource(R.drawable.ic_mic)
                backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary_color)
            }
        }
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO_PERMISSION
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        speechHelper.destroy()
    }
}

// 2. 장비명 입력 Fragment (음성 지원)
class EquipmentNameFragmentV2 : VoiceEnabledFragment() {

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
        val fabMic = view.findViewById<FloatingActionButton>(R.id.fabMic)

        // 음성 입력 설정
        setupVoiceInput(etEquipmentName, fabMic)

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

    fun getEquipmentName(): String = etEquipmentName.text.toString().trim()
}

// 3. 용도 입력 Fragment (음성 지원)
class PurposeFragmentV2 : VoiceEnabledFragment() {

    private lateinit var etPurpose: EditText
    private lateinit var tvExamples: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_purpose_v2, container, false)

        etPurpose = view.findViewById(R.id.etPurpose)
        tvExamples = view.findViewById(R.id.tvExamples)
        val fabMic = view.findViewById<FloatingActionButton>(R.id.fabMic)

        // 음성 입력 설정
        setupVoiceInput(etPurpose, fabMic)

        // 예시 표시
        tvExamples.text = """
            💡 예시:
            • 사료 배합 작업
            • 트랙터 수리
            • 축사 환경 개선
            • 장비 교체
            
            🎤 마이크 버튼을 눌러 음성으로 입력할 수 있습니다
        """.trimIndent()

        return view
    }

    fun getPurpose(): String = etPurpose.text.toString().trim()
}

// 4. 기타사항 입력 Fragment (음성 지원)
class NoteFragmentV2 : VoiceEnabledFragment() {

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
        val fabMic = view.findViewById<FloatingActionButton>(R.id.fabMic)

        // 음성 입력 설정
        setupVoiceInput(etNote, fabMic)

        tvOptional.text = """
            💡 선택사항입니다.
            
            추가로 전달하실 내용이 있으면 입력해주세요.
            예: 긴급 처리 요청, 특별 요구사항 등
            
            🎤 음성으로도 입력 가능합니다
        """.trimIndent()

        return view
    }

    fun getNote(): String = etNote.text.toString().trim()
}