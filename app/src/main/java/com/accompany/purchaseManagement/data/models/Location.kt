package com.accompany.purchaseManagement.data.models

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 지점/현장 정보 데이터 클래스
 * 각 지점의 정보와 구매 담당자를 관리
 */
data class Location(
    @PropertyName("id")
    @SerializedName("ID")
    val id: String = "",
    
    @PropertyName("name")
    @SerializedName("지점명")
    val name: String = "",
    
    @PropertyName("code")
    @SerializedName("지점코드")
    val code: String = "",
    
    @PropertyName("address")
    @SerializedName("주소")
    val address: String = "",
    
    @PropertyName("phoneNumber")
    @SerializedName("전화번호")
    val phoneNumber: String? = null,
    
    @PropertyName("primaryManagerEmail")
    @SerializedName("주담당자이메일")
    val primaryManagerEmail: String = "",
    
    @PropertyName("primaryManagerName")
    @SerializedName("주담당자명")
    val primaryManagerName: String = "",
    
    @PropertyName("secondaryManagerEmails")
    @SerializedName("부담당자이메일목록")
    val secondaryManagerEmails: List<String> = emptyList(),
    
    @PropertyName("secondaryManagerNames")
    @SerializedName("부담당자명목록")
    val secondaryManagerNames: List<String> = emptyList(),
    
    @PropertyName("supervisorEmails")
    @SerializedName("상급자이메일목록")
    val supervisorEmails: List<String> = emptyList(),
    
    @PropertyName("supervisorNames")
    @SerializedName("상급자명목록")
    val supervisorNames: List<String> = emptyList(),
    
    @PropertyName("isActive")
    @SerializedName("활성상태")
    val isActive: Boolean = true,
    
    @PropertyName("region")
    @SerializedName("지역")
    val region: String = "",
    
    @PropertyName("notes")
    @SerializedName("비고")
    val notes: String = "",
    
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    
    /**
     * 모든 이메일 수신자 목록 (주담당자 + 부담당자 + 상급자)
     */
    fun getAllRecipientEmails(): List<String> {
        val emails = mutableListOf<String>()
        
        // 주담당자 (필수)
        if (primaryManagerEmail.isNotEmpty()) {
            emails.add(primaryManagerEmail)
        }
        
        // 부담당자들
        emails.addAll(secondaryManagerEmails)
        
        // 상급자들
        emails.addAll(supervisorEmails)
        
        return emails.distinct() // 중복 제거
    }
    
    /**
     * 주담당자와 부담당자 이메일 목록
     */
    fun getManagerEmails(): List<String> {
        val emails = mutableListOf<String>()
        
        if (primaryManagerEmail.isNotEmpty()) {
            emails.add(primaryManagerEmail)
        }
        
        emails.addAll(secondaryManagerEmails)
        
        return emails.distinct()
    }
    
    /**
     * 모든 담당자와 상급자 이름 목록
     */
    fun getAllRecipientNames(): String {
        val names = mutableListOf<String>()
        
        if (primaryManagerName.isNotEmpty()) {
            names.add("$primaryManagerName (주담당자)")
        }
        
        secondaryManagerNames.forEach { name ->
            names.add("$name (부담당자)")
        }
        
        supervisorNames.forEach { name ->
            names.add("$name (상급자)")
        }
        
        return names.joinToString(", ")
    }
    
    /**
     * 지점 정보 요약
     */
    fun getSummary(): String {
        return "$name ($code) - ${region}"
    }
    
    /**
     * Firebase에서 사용할 수 있는 Map 형태로 변환
     */
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "code" to code,
            "address" to address,
            "phoneNumber" to phoneNumber,
            "primaryManagerEmail" to primaryManagerEmail,
            "primaryManagerName" to primaryManagerName,
            "secondaryManagerEmails" to secondaryManagerEmails,
            "secondaryManagerNames" to secondaryManagerNames,
            "supervisorEmails" to supervisorEmails,
            "supervisorNames" to supervisorNames,
            "isActive" to isActive,
            "region" to region,
            "notes" to notes,
            "createdAt" to createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
    }
    
    /**
     * 유효성 검사
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) errors.add("지점명을 입력해주세요")
        if (code.isBlank()) errors.add("지점코드를 입력해주세요")
        if (primaryManagerEmail.isBlank()) errors.add("주담당자 이메일을 입력해주세요")
        if (primaryManagerName.isBlank()) errors.add("주담당자 이름을 입력해주세요")
        
        // 이메일 형식 검증
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        if (primaryManagerEmail.isNotEmpty() && !emailPattern.matcher(primaryManagerEmail).matches()) {
            errors.add("주담당자 이메일 형식이 올바르지 않습니다")
        }
        
        secondaryManagerEmails.forEach { email ->
            if (email.isNotEmpty() && !emailPattern.matcher(email).matches()) {
                errors.add("부담당자 이메일 형식이 올바르지 않습니다: $email")
            }
        }
        
        supervisorEmails.forEach { email ->
            if (email.isNotEmpty() && !emailPattern.matcher(email).matches()) {
                errors.add("상급자 이메일 형식이 올바르지 않습니다: $email")
            }
        }
        
        return errors
    }
}
