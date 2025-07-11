package com.accompany.purchaseManagement

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class GoogleSheetsHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSheetsHelper"

        // Google Apps Script 웹앱 URL (AppConfig.kt에서 가져옴)
        private val WEBAPP_URL = AppConfig.GOOGLE_SHEETS_URL
    }

    // Google Sheets에 구매신청 데이터 전송
    suspend fun submitToGoogleSheets(
        applicantName: String,
        applicantDepartment: String,
        equipmentName: String,
        location: String,
        purpose: String,
        note: String,
        requestDate: String,
        hasPhoto: Boolean = false,
        photoUrls: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        // ... buildString { append ... } ...
        val postData = buildString {
            // 기존 append들...
            append("&photoUrls=").append(URLEncoder.encode(photoUrls, "UTF-8"))
        }

        try {
            Log.d(TAG, "Google Sheets 전송 시작: $applicantName (사진: $hasPhoto)")

            val url = URL(WEBAPP_URL)
            val connection = url.openConnection() as HttpURLConnection

            // 연결 설정
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            connection.setRequestProperty("User-Agent", "PurchaseRequestApp/${AppConfig.APP_VERSION}")
            connection.doOutput = true
            connection.connectTimeout = 15000 // 15초 타임아웃
            connection.readTimeout = 15000 // 15초 타임아웃

            // POST 데이터 준비 (UTF-8 인코딩)
            val postData = buildString {
                append("applicantName=").append(URLEncoder.encode(applicantName, "UTF-8"))
                append("&applicantDepartment=").append(URLEncoder.encode(applicantDepartment, "UTF-8"))
                append("&equipmentName=").append(URLEncoder.encode(equipmentName, "UTF-8"))
                append("&location=").append(URLEncoder.encode(location, "UTF-8"))
                append("&purpose=").append(URLEncoder.encode(purpose, "UTF-8"))
                append("&note=").append(URLEncoder.encode(note, "UTF-8"))
                append("&requestDate=").append(URLEncoder.encode(requestDate, "UTF-8"))
                append("&status=").append(URLEncoder.encode("대기중", "UTF-8"))
                append("&hasPhoto=").append(URLEncoder.encode(if (hasPhoto) "📸 있음" else "없음", "UTF-8"))

                // 메타 정보 추가
                append("&appVersion=").append(URLEncoder.encode(AppConfig.APP_VERSION, "UTF-8"))
                append("&timestamp=").append(URLEncoder.encode(
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).format(Date()), "UTF-8"
                ))
            }

            if (AppConfig.DEBUG_MODE) {
                Log.d(TAG, "전송 데이터 크기: ${postData.length} bytes")
            }

            // 데이터 전송
            val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
            writer.write(postData)
            writer.flush()
            writer.close()

            // 응답 확인
            val responseCode = connection.responseCode
            Log.d(TAG, "HTTP 응답 코드: $responseCode")

            // 응답 내용 읽기 (디버깅용)
            val response = if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 302) {
                connection.inputStream?.bufferedReader()?.use { it.readText() } ?: ""
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "오류 응답 없음"
            }

            if (AppConfig.DEBUG_MODE) {
                Log.d(TAG, "서버 응답: $response")
            }

            // 성공 여부 판단
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 302) {
                Log.i(TAG, "Google Sheets에 데이터 전송 성공: $applicantName")
                true
            } else {
                Log.e(TAG, "Google Sheets 전송 실패: HTTP $responseCode")
                false
            }

        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Google Sheets 전송 타임아웃: ${e.message}")
            false
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "인터넷 연결 오류: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Google Sheets 전송 중 오류: ${e.message}", e)
            false
        }
    }

    // 연결 테스트용 메소드
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Google Sheets 연결 테스트 시작")

            val url = URL(WEBAPP_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000 // 10초 타임아웃
            connection.readTimeout = 10000 // 10초 타임아웃
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "PurchaseRequestApp/${AppConfig.APP_VERSION}")

            val responseCode = connection.responseCode
            Log.d(TAG, "연결 테스트 응답: $responseCode")

            // 응답 내용 확인
            val response = if (responseCode == HttpURLConnection.HTTP_OK || responseCode == 302) {
                connection.inputStream?.bufferedReader()?.use { it.readText() } ?: ""
            } else {
                ""
            }

            if (AppConfig.DEBUG_MODE) {
                Log.d(TAG, "테스트 응답 내용: $response")
            }

            // Google Apps Script 응답 확인
            val isValid = (responseCode == HttpURLConnection.HTTP_OK || responseCode == 302) &&
                    (response.contains("구매신청") || response.contains("API"))

            if (isValid) {
                Log.i(TAG, "Google Sheets 연결 테스트 성공")
            } else {
                Log.w(TAG, "Google Sheets 연결 테스트 실패 - 응답이 예상과 다름")
            }

            isValid

        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "연결 테스트 타임아웃: ${e.message}")
            false
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "연결 테스트 - 인터넷 연결 오류: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "연결 테스트 실패: ${e.message}")
            false
        }
    }
}