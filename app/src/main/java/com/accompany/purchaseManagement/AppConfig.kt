package com.accompany.purchaseManagement

/**
 * 구매신청 앱 설정값 관리
 *
 * ⚠️ 실제 사용 전에 반드시 아래 값들을 실제 값으로 변경해야 합니다!
 */
object AppConfig {

    // 📧 관리자 이메일 주소 (사진 전송용)
    // 🔥 실제 사용시 실제 관리자 이메일로 변경 필수!
    const val MANAGER_EMAIL = "clrns1234@gmail.com"

    // 📊 Google Apps Script 웹앱 URL
    // 🔥 Google Apps Script 설정 후 실제 URL로 변경 필수!
    // 예시: "https://script.google.com/macros/s/AKfycby123ABC.../exec"
    const val GOOGLE_SHEETS_URL = "https://script.google.com/macros/s/AKfycbyJH2s2lLmzJTVXcvX_320chCpZTkAZoKlPF_asMSXUf8ej8Xvhcp5uTwAkcte26wbVmw/exec"

    // 📅 자동 데이터 정리 기간 (개월)
    const val AUTO_CLEANUP_MONTHS = 1000

    // 📱 앱 버전
    const val APP_VERSION = "1.0"

    // 🔧 디버그 모드 (개발시에만 true, 배포시 false)
    const val DEBUG_MODE = true

    // 📐 UI 설정
    const val BUTTON_TEXT_SIZE = 24 // sp
    const val LARGE_BUTTON_HEIGHT = 80 // dp
    const val LARGE_BUTTON_WIDTH = 280 // dp

    // 🔗 도움말 URL (선택사항)
    const val HELP_URL = "https://docs.google.com/document/d/https://docs.google.com/document/d/13bmlpzMjyv77LrJvbvTM42j2tnJJ8a5UZVQqkyLuX1E/edit?tab=t.0/edit"

    /**
     * 설정 검증 메소드
     * 앱 시작시 설정값이 올바른지 확인
     */
    fun validateConfig(): List<String> {
        val errors = mutableListOf<String>()

        // 이메일 검증 - 기본값에서 변경되었는지 확인
        if (MANAGER_EMAIL == "clrns1234@gmail.com") {
            errors.add("⚠️ MANAGER_EMAIL을 실제 관리자 이메일로 변경해주세요")
        }

        // Google Sheets URL 검증 - 기본값에서 변경되었는지 확인
        if (GOOGLE_SHEETS_URL.contains("https://script.google.com/macros/s/AKfycbyJH2s2lLmzJTVXcvX_320chCpZTkAZoKlPF_asMSXUf8ej8Xvhcp5uTwAkcte26wbVmw/exec")) {
            errors.add("⚠️ GOOGLE_SHEETS_URL을 실제 Google Apps Script URL로 변경해주세요")
        }

        // URL 형식 검증 - 올바른 Apps Script URL 형식인지 확인
        if (!GOOGLE_SHEETS_URL.startsWith("https://script.google.com/macros/s/AKfycbyJH2s2lLmzJTVXcvX_320chCpZTkAZoKlPF_asMSXUf8ej8Xvhcp5uTwAkcte26wbVmw/exec")) {

            errors.add("⚠️ GOOGLE_SHEETS_URL 형식이 올바르지 않습니다")
        }

        return errors
    }

    /**
     * 설정 상태 요약
     */
    fun getConfigSummary(): String {
        return """
            📧 관리자 이메일: $MANAGER_EMAIL
            📊 Google Sheets: ${if (GOOGLE_SHEETS_URL.contains("YOUR_SCRIPT_ID_HERE")) "❌ 미설정" else "✅ 설정됨"}
            📅 데이터 정리: ${AUTO_CLEANUP_MONTHS}개월
            📱 앱 버전: $APP_VERSION
            🔧 디버그: ${if (DEBUG_MODE) "ON" else "OFF"}
        """.trimIndent()
    }
}