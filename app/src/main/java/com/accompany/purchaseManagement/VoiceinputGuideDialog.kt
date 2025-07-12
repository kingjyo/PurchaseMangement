package com.accompany.purchaseManagement

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog

object VoiceInputGuideDialog {

    private const val PREF_NAME = "VoiceGuidePrefs"
    private const val KEY_SHOWN = "voice_guide_shown"

    fun showIfFirstTime(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val hasShown = prefs.getBoolean(KEY_SHOWN, false)

        if (!hasShown) {
            show(context)
            prefs.edit().putBoolean(KEY_SHOWN, true).apply()
        }
    }

    fun show(context: Context) {
        val message = """
            🎤 음성 입력 기능이 추가되었습니다!
            
            사용 방법:
            1. 각 입력 화면의 마이크 버튼을 탭하세요
            2. 말씀하시면 자동으로 텍스트로 변환됩니다
            3. 연속으로 말씀하실 수 있습니다
            4. 다시 탭하면 음성 입력이 종료됩니다
            
            💡 팁:
            • 천천히 또박또박 말씀해주세요
            • 조용한 곳에서 사용하면 더 정확합니다
            • 수정이 필요하면 키보드로 편집하세요
            
            음성 입력 기능을 사용하시려면 마이크 권한이 필요합니다.
        """.trimIndent()

        AlertDialog.Builder(context)
            .setTitle("🎤 음성 입력 안내")
            .setMessage(message)
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("다시 보지 않기") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}