package com.accompany.purchaseManagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import java.util.*

class SpeechRecognitionHelper(private val activity: Activity) {

    companion object {
        private const val TAG = "SpeechRecognition"
    }

    private var speechRecognizer: SpeechRecognizer? = null

    // 콜백 인터페이스
    interface SpeechRecognitionCallback {
        fun onResults(text: String)
        fun onError(error: String)
        fun onReadyForSpeech()
        fun onEndOfSpeech()
    }

    private var callback: SpeechRecognitionCallback? = null

    fun setCallback(callback: SpeechRecognitionCallback) {
        this.callback = callback
    }

    // 음성 인식 가능 여부 확인
    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(activity)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            // Handle the result
        }
    }

    // 단일 음성 인식 시작
    fun startSingleRecognition() {
        if (!isRecognitionAvailable()) {
            Toast.makeText(activity, "음성 인식을 사용할 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        // 음성 인식 시작
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")  // 한국어
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.packageName)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀해주세요")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            activity.startActivityForResult(intent, 1000)  // 음성 인식 요청
        } catch (e: Exception) {
            Toast.makeText(activity, "음성 인식 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // SpeechRecognizer 설정
    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
            setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "음성 입력 준비됨")
                    callback?.onReadyForSpeech()
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "음성 입력 시작")
                }

                override fun onRmsChanged(rmsdB: Float) {}

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
                    callback?.onError(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        Log.d(TAG, "인식 결과: $text")
                        callback?.onResults(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

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
        }

        speechRecognizer?.startListening(intent)
    }

    // 음성 인식 중지
    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // 리소스 정리
    fun destroy() {
        stopListening()
    }

    // Activity Result 처리 (더 이상 사용되지 않음)
    // 음성 인식은 RecognitionListener를 통해 처리되므로 이 부분은 제거할 수 있습니다.
}
