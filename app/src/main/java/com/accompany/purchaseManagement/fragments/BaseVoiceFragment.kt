package com.accompany.purchaseManagement.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.accompany.purchaseManagement.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

/**
 * 음성 입력 기능을 제공하는 기본 Fragment
 */
abstract class BaseVoiceFragment : Fragment() {
    
    companion object {
        private const val TAG = "BaseVoiceFragment"
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    protected var targetEditText: EditText? = null
    protected var micButton: View? = null
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSpeechRecognizer()
    }
    
    private fun initializeSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Log.e(TAG, "Speech recognition is not available")
            Toast.makeText(context, "음성 인식을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizer?.setRecognitionListener(createRecognitionListener())
    }
    
    protected fun setupVoiceInput(editText: EditText, micButton: View) {
        this.targetEditText = editText
        this.micButton = micButton
        
        micButton.setOnClickListener {
            if (checkAudioPermission()) {
                toggleListening()
            } else {
                requestAudioPermission()
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
    
    private fun toggleListening() {
        if (isListening) {
            stopListening()
        } else {
            startListening()
        }
    }
    
    private fun startListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀해 주세요")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            speechRecognizer?.startListening(intent)
            isListening = true
            updateMicButtonState(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            Toast.makeText(context, "음성 인식을 시작할 수 없습니다", Toast.LENGTH_SHORT).show()
            isListening = false
            updateMicButtonState(false)
        }
    }
    
    private fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            isListening = false
            updateMicButtonState(false)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }
    
    private fun updateMicButtonState(isActive: Boolean) {
        when (micButton) {
            is ImageButton -> {
                (micButton as ImageButton).setImageResource(
                    if (isActive) R.drawable.ic_mic_active else R.drawable.ic_mic
                )
            }
            is FloatingActionButton -> {
                (micButton as FloatingActionButton).setImageResource(
                    if (isActive) R.drawable.ic_mic_active else R.drawable.ic_mic
                )
            }
        }
    }
    
    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
        }
        
        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Speech started")
        }
        
        override fun onRmsChanged(rmsdB: Float) {
            // 음성 레벨 변화 (필요시 UI 업데이트)
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {}
        
        override fun onEndOfSpeech() {
            Log.d(TAG, "Speech ended")
            isListening = false
            updateMicButtonState(false)
        }
        
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 녹음 오류"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 오류"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한이 부족합니다"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간 초과"
                SpeechRecognizer.ERROR_NO_MATCH -> "일치하는 결과 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "인식기가 사용 중입니다"
                SpeechRecognizer.ERROR_SERVER -> "서버 오류"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간 초과"
                else -> "알 수 없는 오류"
            }
            
            Log.e(TAG, "Speech recognition error: $errorMessage (code: $error)")
            
            // ERROR_NO_MATCH는 사용자에게 표시하지 않음 (말을 하지 않은 경우)
            if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            
            isListening = false
            updateMicButtonState(false)
        }
        
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                Log.d(TAG, "Recognized: $recognizedText")
                
                targetEditText?.let { editText ->
                    val currentText = editText.text.toString()
                    val newText = if (currentText.isEmpty()) {
                        recognizedText
                    } else {
                        "$currentText $recognizedText"
                    }
                    editText.setText(newText)
                    editText.setSelection(newText.length)
                }
            }
            
            isListening = false
            updateMicButtonState(false)
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                Log.d(TAG, "Partial: ${matches[0]}")
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Audio permission granted")
                Toast.makeText(context, "음성 입력 권한이 허용되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "Audio permission denied")
                Toast.makeText(context, "음성 입력을 사용하려면 권한이 필요합니다", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        try {
            if (isListening) {
                stopListening()
            }
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer", e)
        }
        
        targetEditText = null
        micButton = null
    }
    
    override fun onPause() {
        super.onPause()
        if (isListening) {
            stopListening()
        }
    }
}