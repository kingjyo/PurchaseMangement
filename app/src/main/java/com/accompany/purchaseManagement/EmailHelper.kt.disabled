package com.accompany.purchaseManagement

import com.google.api.client.Gmail;
import java.util.Base64;
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailHelper(private val gmailService: Gmail) {

    // 이메일 본문을 생성하는 함수
    fun createEmail(to: String, from: String, subject: String, bodyText: String, photoUrls: List<String>): Message {
        val emailContent = """
            <html>
                <body>
                    <h2>$subject</h2>
                    <p><strong>신청자:</strong> $bodyText</p>
                    <p><strong>부서:</strong> department</p>
                    <p><strong>신청 품목:</strong> equipmentName</p>
                    <p><strong>수량:</strong> quantity</p>
                    <p><strong>장소:</strong> location</p>
                    <p><strong>목적:</strong> purpose</p>
                    <p><strong>기타사항:</strong> note</p>
                    
                    <!-- 사진 URL을 이미지로 삽입 -->
                    <p><strong>첨부 이미지:</strong></p>
                    <img src="${photoUrls.firstOrNull()}" alt="photo" width="200" height="200"/>
                </body>
            </html>
        """.trimIndent()

        val mimeMessage = MimeMessage(Session.getDefaultInstance(System.getProperties(), null))
        mimeMessage.setFrom(from)
        mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO, to)
        mimeMessage.subject = subject

        // 이메일 본문
        val multipart = MimeMultipart()

        val bodyPart = MimeBodyPart()
        bodyPart.setContent(emailContent, "text/html; charset=UTF-8")
        multipart.addBodyPart(bodyPart)

        mimeMessage.setContent(multipart)

        val rawMessage = Base64.getUrlEncoder().encodeToString(mimeMessage.toString().toByteArray())

        val message = Message()
        message.raw = rawMessage

        return message
    }

    // 이메일 보내는 함수
    fun sendEmail(to: String, from: String, subject: String, bodyText: String, photoUrls: List<String>) {
        val emailMessage = createEmail(to, from, subject, bodyText, photoUrls)
        try {
            val sendMessage = gmailService.users().messages().send("me", emailMessage).execute()
            println("Email sent: $sendMessage")
        } catch (e: MessagingException) {
            println("Error sending email: ${e.message}")
        }
    }
}
