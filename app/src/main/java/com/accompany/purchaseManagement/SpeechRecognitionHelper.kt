package com.accompany.purchaseManagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import java.util.*
import android.os.Handler
import android.os.Looper

class SpeechRecognitionHelper(private val activity: Activity) {

    companion object {
        private const val TAG = "SpeechRecognition"
        const val REQUEST_CODE_SPEECH_INPUT = 1000
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isContinuousMode = false

    // 콜백 인터페이스
    interface SpeechRecognitionCallback {
        fun onResults(text: String)
        fun onError(error: String)
        fun onReadyForSpeech()
        fun onEndOfSpeech()
        fun onPartialResults(partialText: String)
    }

    private var callback: SpeechRecognitionCallback? = null

    fun setCallback(callback: SpeechRecognitionCallback) {
        this.callback = callback
    }

    // 음성 인식 가능 여부 확인
    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(activity)
    }

    // 단일 음성 인식 시작 (Intent 방식)
    fun startSingleRecognition() {
        if (!isRecognitionAvailable()) {
            Toast.makeText(activity, "음성 인식을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀해주세요")
        }

        try {
            activity.startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(activity, "음성 인식 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 연속 음성 인식 시작/중지
    fun toggleContinuousRecognition() {
        if (isListening) {
            stopListening()
        } else {
            startContinuousRecognition()
        }
    }

    // 연속 음성 인식 시작
    private fun startContinuousRecognition() {
        if (!isRecognitionAvailable()) {
            callback?.onError("음성 인식을 사용할 수 없습니다")
            return
        }

        isContinuousMode = true
        setupSpeechRecognizer()
        startListening()
    }

    // SpeechRecognizer 설정
    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "음성 입력 준비됨")
                    callback?.onReadyForSpeech()
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "음성 입력 시작")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // 음량 레벨 변화 (필요시 UI 업데이트)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    Log.d(TAG, "음성 입력 종료")
                    callback?.onEndOfSpeech()
                }

                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "오디오 녹음 오류"
                        SpeechRecognizer.ERROR_CLIENT -> "클라이언트 오류"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한이 없습니다"
                        SpeechRecognizer.ERROR_NETWORK -> "네트워크 오류"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 시간초과"
                        SpeechRecognizer.ERROR_NO_MATCH -> "인식 결과 없음"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "인식기가 바쁩니다"
                        SpeechRecognizer.ERROR_SERVER -> "서버 오류"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 시간초과"
                        else -> "알 수 없는 오류"
                    }

                    Log.e(TAG, "음성 인식 오류: $errorMessage")

                    // 연속 모드에서는 오류 후 자동 재시작
                    if (isContinuousMode && isListening) {
                        when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                                // 음성이 감지되지 않은 경우 재시작
                                activity.runOnUiThread {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        if (isListening) {
                                            startListening()
                                        }
                                    }, 100)
                                }
                            }
                            else -> {
                                callback?.onError(errorMessage)
                                stopListening()
                            }
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        Log.d(TAG, "인식 결과: $text")
                        callback?.onResults(text)

                        // 연속 모드에서는 자동으로 다시 시작
                        if (isContinuousMode && isListening) {
                            activity.runOnUiThread {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    if (isListening) {
                                        startListening()
                                    }
                                }, 500)
                            }
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        callback?.onPartialResults(matches[0])
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    // 음성 인식 시작
    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500)
        }

        speechRecognizer?.startListening(intent)
        isListening = true
    }

    // 음성 인식 중지
    fun stopListening() {
        isListening = false
        isContinuousMode = false
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // 리소스 정리
    fun destroy() {
        stopListening()
    }

    // Activity Result 처리
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!result.isNullOrEmpty()) {
                callback?.onResults(result[0])
            }
        }
    }
}

