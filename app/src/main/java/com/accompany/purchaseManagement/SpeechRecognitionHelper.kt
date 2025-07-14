package com.accompany.purchaseManagement

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import android.os.Bundle

class SpeechRecognitionHelper(
    private val activity: Activity,
    private val launcher: ActivityResultLauncher<Intent>? = null  // Fragment에서 전달된 launcher
) {

    private var callback: SpeechRecognitionCallback? = null
    private var speechRecognizer: SpeechRecognizer? = null  // SpeechRecognizer 객체

    companion object {
        private const val TAG = "SpeechRecognition"
    }

    // 콜백 인터페이스
    interface SpeechRecognitionCallback {
        fun onResults(text: String)
        fun onError(error: String)
        fun onReadyForSpeech()
        fun onEndOfSpeech()
    }

    // 콜백 설정
    fun setCallback(callback: SpeechRecognitionCallback) {
        this.callback = callback
    }

    // 음성 인식 가능 여부 확인
    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(activity)
    }

    // 음성 인식 시작
    fun startSingleRecognition() {
        if (!isRecognitionAvailable()) {
            Toast.makeText(activity, "음성 인식을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        // SpeechRecognizer 초기화
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
            setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    callback?.onReadyForSpeech()
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
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
                    callback?.onError(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        callback?.onResults(matches[0])  // 첫 번째 텍스트 전달
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        // 음성 인식 인텐트 설정
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")  // 한국어
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.packageName)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀해주세요")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            // launcher가 있으면 launcher를 사용하여 음성 인식 시작
            launcher?.launch(intent) ?: activity.startActivityForResult(intent, 1000)
        } catch (e: Exception) {
            Toast.makeText(activity, "음성 인식 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            callback?.onError("음성 인식 시작 실패: ${e.message}")
        }
    }

    // stopListening() 메서드 추가: 음성 인식 중지
    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
    }

    // 리소스 정리 메서드
    fun destroy() {
        stopListening()
        speechRecognizer?.destroy() // SpeechRecognizer 리소스를 해제
        speechRecognizer = null  // 참조 제거
    }

    // ActivityResult 처리 (음성 인식 결과 처리)
    fun handleActivityResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                callback?.onResults(results[0])  // 인식된 첫 번째 텍스트를 콜백으로 전달
            }
        } else {
            callback?.onError("음성 인식 취소됨")
        }
    }
}