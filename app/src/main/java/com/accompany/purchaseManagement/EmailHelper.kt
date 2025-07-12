package com.accompany.purchaseManagement

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class EmailHelper(private val context: Context) {

    companion object {
        private const val TAG = "EmailHelper"

        // Google Apps Script 이메일 전송 URL (별도로 만들어야 함)
        private const val EMAIL_SCRIPT_URL = "YOUR_EMAIL_SCRIPT_URL_HERE"
    }

    // 구매신청 이메일 전송
    suspend fun sendPurchaseRequestEmail(
        applicantName: String,
        applicantDepartment: String,
        equipmentName: String,
        quantity: String,
        location: String,
        purpose: String,
        note: String,
        requestDate: String,
        photoUrls: List<String>
    ): Boolean = withContext(Dispatchers.IO) {

        try {
            // HTML 이메일 본문 생성
            val emailBody = createEmailBody(
                applicantName, applicantDepartment, equipmentName,
                quantity, location, purpose, note, requestDate, photoUrls
            )

            // POST 데이터 준비
            val postData = buildString {
                append("to=").append(URLEncoder.encode(AppConfig.MANAGER_EMAIL, "UTF-8"))
                append("&subject=").append(URLEncoder.encode(
                    "[구매신청] $applicantName - $equipmentName", "UTF-8"))
                append("&body=").append(URLEncoder.encode(emailBody, "UTF-8"))
                append("&isHtml=").append("true")
            }

            // HTTP 요청
            val url = URL(EMAIL_SCRIPT_URL)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            // 데이터 전송
            connection.outputStream.use { os ->
                os.write(postData.toByteArray())
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "이메일 전송 응답 코드: $responseCode")

            responseCode == HttpURLConnection.HTTP_OK || responseCode == 302

        } catch (e: Exception) {
            Log.e(TAG, "이메일 전송 실패", e)
            false
        }
    }

    // HTML 이메일 본문 생성
    private fun createEmailBody(
        applicantName: String,
        applicantDepartment: String,
        equipmentName: String,
        quantity: String,
        location: String,
        purpose: String,
        note: String,
        requestDate: String,
        photoUrls: List<String>
    ): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background-color: #f9f9f9; padding: 30px; border: 1px solid #ddd; border-radius: 0 0 10px 10px; }
                    .info-row { margin-bottom: 15px; padding: 10px; background-color: white; border-radius: 5px; }
                    .label { font-weight: bold; color: #555; display: inline-block; width: 120px; }
                    .value { color: #333; }
                    .photos { margin-top: 30px; }
                    .photo-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-top: 10px; }
                    .photo-item { width: 100%; max-width: 280px; height: auto; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                    .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #666; }
                    .important { background-color: #fff3cd; border: 1px solid #ffeeba; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>🛒 새로운 구매신청이 접수되었습니다</h2>
                    </div>
                    
                    <div class="content">
                        <div class="important">
                            <strong>📌 신청자:</strong> ${applicantName} (${applicantDepartment})<br>
                            <strong>📦 구매 물품:</strong> ${equipmentName} (${quantity}개)
                        </div>
                        
                        <div class="info-row">
                            <span class="label">👤 신청자</span>
                            <span class="value">${applicantName}</span>
                        </div>
                        
                        <div class="info-row">
                            <span class="label">🏢 소속</span>
                            <span class="value">${applicantDepartment}</span>
                        </div>
                        
                        <div class="info-row">
                            <span class="label">📦 구매 물품/부품</span>
                            <span class="value">${equipmentName}</span>
                        </div>
                        
                        <div class="info-row">
                            <span class="label">🔢 수량</span>
                            <span class="value">${quantity}개</span>
                        </div>
                        
                        ${if (location.isNotEmpty()) """
                        <div class="info-row">
                            <span class="label">📍 사용 장소/장비</span>
                            <span class="value">${location}</span>
                        </div>
                        """ else ""}
                        
                        <div class="info-row">
                            <span class="label">📝 사용 용도</span>
                            <span class="value">${purpose}</span>
                        </div>
                        
                        ${if (note.isNotEmpty()) """
                        <div class="info-row">
                            <span class="label">💬 기타사항</span>
                            <span class="value">${note}</span>
                        </div>
                        """ else ""}
                        
                        <div class="info-row">
                            <span class="label">📅 신청일시</span>
                            <span class="value">${requestDate}</span>
                        </div>
                        
                        ${if (photoUrls.isNotEmpty()) """
                        <div class="photos">
                            <h3>📸 첨부 사진 (${photoUrls.size}장)</h3>
                            <div class="photo-grid">
                                ${photoUrls.joinToString("") { url ->
            """<img src="${url}" alt="첨부사진" class="photo-item">"""
        }}
                            </div>
                        </div>
                        """ else """
                        <div class="info-row" style="text-align: center; color: #666;">
                            📷 첨부된 사진이 없습니다
                        </div>
                        """}
                    </div>
                    
                    <div class="footer">
                        <p>이 이메일은 구매신청 시스템에서 자동으로 발송되었습니다.</p>
                        <p>상세 내용은 Google Sheets에서 확인하실 수 있습니다.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    // 상태 변경 알림 이메일 전송
    suspend fun sendStatusChangeEmail(
        requesterEmail: String,
        applicantName: String,
        equipmentName: String,
        oldStatus: String,
        newStatus: String,
        requestDate: String
    ): Boolean = withContext(Dispatchers.IO) {

        try {
            val emailBody = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 500px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background-color: #f9f9f9; padding: 30px; border: 1px solid #ddd; border-radius: 0 0 10px 10px; }
                        .status-box { background-color: white; padding: 20px; border-radius: 5px; text-align: center; margin: 20px 0; }
                        .old-status { color: #666; text-decoration: line-through; }
                        .arrow { font-size: 24px; margin: 0 10px; }
                        .new-status { color: #4CAF50; font-weight: bold; font-size: 18px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>📋 구매신청 상태가 변경되었습니다</h2>
                        </div>
                        
                        <div class="content">
                            <p>안녕하세요, ${applicantName}님</p>
                            <p>신청하신 <strong>${equipmentName}</strong>의 처리 상태가 변경되었습니다.</p>
                            
                            <div class="status-box">
                                <span class="old-status">${oldStatus}</span>
                                <span class="arrow">→</span>
                                <span class="new-status">${newStatus}</span>
                            </div>
                            
                            <p><strong>신청일:</strong> ${requestDate}</p>
                            
                            <hr style="margin: 20px 0; border: none; border-top: 1px solid #ddd;">
                            
                            <p style="text-align: center; color: #666; font-size: 14px;">
                                구매신청 시스템에서 자동 발송된 이메일입니다.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent()

            val postData = buildString {
                append("to=").append(URLEncoder.encode(requesterEmail, "UTF-8"))
                append("&subject=").append(URLEncoder.encode(
                    "[구매신청] ${equipmentName} - 상태 변경: ${newStatus}", "UTF-8"))
                append("&body=").append(URLEncoder.encode(emailBody, "UTF-8"))
                append("&isHtml=").append("true")
            }

            // HTTP 요청 (위와 동일)
            val url = URL(EMAIL_SCRIPT_URL)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            connection.outputStream.use { os ->
                os.write(postData.toByteArray())
            }

            val responseCode = connection.responseCode
            responseCode == HttpURLConnection.HTTP_OK || responseCode == 302

        } catch (e: Exception) {
            Log.e(TAG, "상태 변경 이메일 전송 실패", e)
            false
        }
    }
}