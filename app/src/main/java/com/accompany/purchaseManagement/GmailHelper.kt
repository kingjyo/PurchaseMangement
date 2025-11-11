package com.accompany.purchaseManagement

import android.content.Context
import android.util.Log
import com.accompany.purchaseManagement.data.models.PurchaseRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Properties
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * Gmail API를 사용하여 구매신청 알림 이메일을 전송하는 헬퍼 클래스
 */
class GmailHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "GmailHelper"
        private const val APPLICATION_NAME = "PurchaseManagement"
    }
    
    private var gmailService: Gmail? = null
    
    /**
     * Gmail 서비스 초기화
     * Google 로그인 후 호출해야 함
     */
    fun initializeGmailService(account: GoogleSignInAccount): Boolean {
        return try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(GmailScopes.GMAIL_SEND)
            )
            credential.selectedAccount = account.account
            
            gmailService = Gmail.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(APPLICATION_NAME)
                .build()
            
            Log.d(TAG, "Gmail service initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Gmail service", e)
            false
        }
    }
    
    /**
     * 현재 로그인된 Google 계정으로 Gmail 서비스 초기화
     */
    fun initializeWithCurrentAccount(): Boolean {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                initializeGmailService(account)
            } else {
                Log.w(TAG, "No Google account signed in")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize with current account", e)
            false
        }
    }
    
    /**
     * 구매신청 알림 이메일 전송
     * 
     * @param request 구매신청 정보
     * @param adminEmail 관리자 이메일 주소 (수신자)
     * @return 전송 성공 여부
     */
    suspend fun sendPurchaseRequestEmail(
        request: PurchaseRequest,
        adminEmail: String = AppConfig.MANAGER_EMAIL
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (gmailService == null) {
                if (!initializeWithCurrentAccount()) {
                    return@withContext Result.failure(
                        Exception("Gmail service not initialized. Please login with Google first.")
                    )
                }
            }
            
            val emailContent = createEmailContent(request)
            val mimeMessage = createMimeMessage(
                to = adminEmail,
                subject = "알림: 구매신청 도착",
                bodyHtml = emailContent
            )
            
            val message = createGmailMessage(mimeMessage)
            gmailService?.users()?.messages()?.send("me", message)?.execute()
            
            Log.d(TAG, "Purchase request email sent successfully to $adminEmail")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send purchase request email", e)
            Result.failure(e)
        }
    }
    
    /**
     * 구매신청 정보를 HTML 이메일 본문으로 변환
     */
    private fun createEmailContent(request: PurchaseRequest): String {
        val photoSection = if (request.photoUrls.isNotEmpty()) {
            """
            <div style="margin-top: 20px;">
                <h3 style="color: #2196F3;">📷 첨부된 사진</h3>
                ${request.photoUrls.take(3).joinToString("\n") { photoUrl ->
                    """
                    <div style="margin: 10px 0;">
                        <img src="$photoUrl" alt="구매신청 사진" style="max-width: 400px; border-radius: 8px; border: 1px solid #ddd;" />
                    </div>
                    """
                }}
                ${if (request.photoUrls.size > 3) {
                    "<p style='color: #666;'>그 외 ${request.photoUrls.size - 3}개의 사진이 더 있습니다.</p>"
                } else ""}
            </div>
            """.trimIndent()
        } else {
            "<p style='color: #666; font-style: italic;'>첨부된 사진이 없습니다.</p>"
        }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: 'Malgun Gothic', '맑은 고딕', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px;
                        border-radius: 10px 10px 0 0;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                    }
                    .content {
                        background: #fff;
                        padding: 30px;
                        border: 1px solid #e0e0e0;
                        border-top: none;
                        border-radius: 0 0 10px 10px;
                    }
                    .section {
                        margin: 20px 0;
                        padding: 15px;
                        background: #f9f9f9;
                        border-left: 4px solid #2196F3;
                        border-radius: 4px;
                    }
                    .section h3 {
                        margin-top: 0;
                        color: #2196F3;
                        font-size: 18px;
                    }
                    .info-row {
                        margin: 10px 0;
                        padding: 8px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .info-row:last-child {
                        border-bottom: none;
                    }
                    .label {
                        display: inline-block;
                        font-weight: bold;
                        color: #555;
                        min-width: 100px;
                    }
                    .value {
                        color: #333;
                    }
                    .footer {
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 2px solid #e0e0e0;
                        text-align: center;
                        color: #999;
                        font-size: 12px;
                    }
                    .status-badge {
                        display: inline-block;
                        padding: 5px 15px;
                        background: #FF9800;
                        color: white;
                        border-radius: 20px;
                        font-size: 14px;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🔔 새로운 구매신청이 도착했습니다</h1>
                    <p style="margin: 10px 0 0 0; font-size: 14px;">신청일시: ${request.requestDate}</p>
                </div>
                
                <div class="content">
                    <!-- 신청 상태 -->
                    <div style="text-align: center; margin-bottom: 20px;">
                        <span class="status-badge">${request.status}</span>
                    </div>
                    
                    <!-- 1. 신청자 정보 -->
                    <div class="section">
                        <h3>👤 신청자 정보</h3>
                        <div class="info-row">
                            <span class="label">이름:</span>
                            <span class="value">${request.applicantName}</span>
                        </div>
                        <div class="info-row">
                            <span class="label">소속:</span>
                            <span class="value">${request.applicantDepartment}</span>
                        </div>
                        <div class="info-row">
                            <span class="label">이메일:</span>
                            <span class="value">${request.applicantEmail}</span>
                        </div>
                    </div>
                    
                    <!-- 2. 구매 정보 -->
                    <div class="section">
                        <h3>🛒 구매 정보</h3>
                        <div class="info-row">
                            <span class="label">장비/품목명:</span>
                            <span class="value"><strong>${request.equipmentName}</strong></span>
                        </div>
                        <div class="info-row">
                            <span class="label">수량:</span>
                            <span class="value">${request.quantity}</span>
                        </div>
                        <div class="info-row">
                            <span class="label">장소:</span>
                            <span class="value">${request.location}</span>
                        </div>
                        <div class="info-row">
                            <span class="label">용도:</span>
                            <span class="value">${request.purpose}</span>
                        </div>
                        ${if (request.note.isNotEmpty()) """
                        <div class="info-row">
                            <span class="label">기타사항:</span>
                            <span class="value">${request.note}</span>
                        </div>
                        """ else ""}
                    </div>
                    
                    <!-- 3. 첨부 사진 -->
                    $photoSection
                    
                    <!-- 안내 메시지 -->
                    <div style="margin-top: 30px; padding: 20px; background: #E3F2FD; border-radius: 8px;">
                        <p style="margin: 0; color: #1976D2;">
                            <strong>📱 앱에서 확인하기:</strong><br/>
                            구매신청 관리 앱을 열어 자세한 내용을 확인하고 승인/거부 처리를 진행해주세요.
                        </p>
                    </div>
                </div>
                
                <div class="footer">
                    <p>본 메일은 구매신청 관리 시스템에서 자동으로 발송되었습니다.</p>
                    <p>문의사항이 있으시면 시스템 관리자에게 연락해주세요.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * MIME 메시지 생성
     */
    private fun createMimeMessage(
        to: String,
        subject: String,
        bodyHtml: String
    ): MimeMessage {
        val props = Properties()
        val session = Session.getDefaultInstance(props, null)
        val message = MimeMessage(session)
        
        // 발신자는 현재 로그인된 Google 계정
        message.setFrom(InternetAddress("me"))
        message.addRecipient(
            javax.mail.Message.RecipientType.TO,
            InternetAddress(to)
        )
        message.subject = subject
        
        // HTML 본문
        val multipart = MimeMultipart()
        val htmlPart = MimeBodyPart()
        htmlPart.setContent(bodyHtml, "text/html; charset=UTF-8")
        multipart.addBodyPart(htmlPart)
        
        message.setContent(multipart)
        
        return message
    }
    
    /**
     * MIME 메시지를 Gmail API Message로 변환
     */
    private fun createGmailMessage(mimeMessage: MimeMessage): Message {
        val buffer = ByteArrayOutputStream()
        mimeMessage.writeTo(buffer)
        val bytes = buffer.toByteArray()
        val encodedEmail = android.util.Base64.encodeToString(
            bytes,
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )
        
        val message = Message()
        message.raw = encodedEmail
        return message
    }
    
    /**
     * Gmail 서비스가 초기화되었는지 확인
     */
    fun isInitialized(): Boolean = gmailService != null
    
    /**
     * 테스트 이메일 전송
     */
    suspend fun sendTestEmail(to: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (gmailService == null) {
                if (!initializeWithCurrentAccount()) {
                    return@withContext Result.failure(
                        Exception("Gmail service not initialized")
                    )
                }
            }
            
            val testHtml = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #4CAF50;">✅ 테스트 메일</h2>
                    <p>구매신청 관리 앱의 Gmail 연동이 정상적으로 작동하고 있습니다.</p>
                    <p>발송 시간: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}</p>
                </body>
                </html>
            """.trimIndent()
            
            val mimeMessage = createMimeMessage(
                to = to,
                subject = "구매신청 앱 - 테스트 메일",
                bodyHtml = testHtml
            )
            
            val message = createGmailMessage(mimeMessage)
            gmailService?.users()?.messages()?.send("me", message)?.execute()
            
            Log.d(TAG, "Test email sent successfully to $to")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send test email", e)
            Result.failure(e)
        }
    }
}
