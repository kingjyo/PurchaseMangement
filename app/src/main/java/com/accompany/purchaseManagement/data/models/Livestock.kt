package com.accompany.purchaseManagement.data.models

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 통합된 가축 정보 데이터 클래스
 * Firebase Firestore, Google Sheets 모두에서 사용 가능
 */
data class Livestock(
    @PropertyName("id")
    @SerializedName("ID")
    val id: String = "",
    
    @PropertyName("localId")
    val localId: Long = 0L, // 로컬 DB용 ID
    
    @PropertyName("earTag")
    @SerializedName("귀표번호")
    val earTag: String = "",
    
    @PropertyName("species")
    @SerializedName("축종")
    val species: String = "",
    
    @PropertyName("breed")
    @SerializedName("품종")
    val breed: String = "",
    
    @PropertyName("gender")
    @SerializedName("성별")
    val gender: String = "",
    
    @PropertyName("birthDate")
    @SerializedName("출생일")
    val birthDate: String = "",
    
    @PropertyName("weight")
    @SerializedName("체중")
    val weight: String = "",
    
    @PropertyName("mother")
    @SerializedName("모축")
    val mother: String = "",
    
    @PropertyName("father")
    @SerializedName("부축")
    val father: String = "",
    
    @PropertyName("location")
    @SerializedName("위치")
    val location: String = "",
    
    @PropertyName("healthStatus")
    @SerializedName("건강상태")
    val healthStatus: String = HEALTH_NORMAL,
    
    @PropertyName("note")
    @SerializedName("비고")
    val note: String = "",
    
    @PropertyName("photoUrls")
    @SerializedName("사진URL")
    val photoUrls: List<String> = emptyList(),
    
    @PropertyName("ownerId")
    @SerializedName("소유자ID")
    val ownerId: String = "",
    
    @PropertyName("ownerName")
    @SerializedName("소유자명")
    val ownerName: String = "",
    
    @PropertyName("registrationDate")
    @SerializedName("등록일시")
    val registrationDate: String = "",
    
    @PropertyName("isActive")
    @SerializedName("활성상태")
    val isActive: Boolean = true,
    
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
    
) : Serializable {
    
    companion object {
        const val SPECIES_CATTLE = "소"
        const val SPECIES_PIG = "돼지"
        const val SPECIES_CHICKEN = "닭"
        const val SPECIES_GOAT = "염소"
        const val SPECIES_SHEEP = "양"
        
        const val GENDER_MALE = "수컷"
        const val GENDER_FEMALE = "암컷"
        const val GENDER_CASTRATED = "거세"
        
        const val HEALTH_NORMAL = "정상"
        const val HEALTH_SICK = "질병"
        const val HEALTH_INJURED = "부상"
        const val HEALTH_PREGNANT = "임신"
        const val HEALTH_DEAD = "폐사"
    }
    
    /**
     * 나이 계산 (개월 수)
     */
    fun getAgeInMonths(): Int {
        if (birthDate.isEmpty()) return 0
        
        return try {
            val birth = java.time.LocalDate.parse(birthDate)
            val now = java.time.LocalDate.now()
            java.time.Period.between(birth, now).toTotalMonths().toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 건강 상태에 따른 색상
     */
    fun getHealthStatusColor(): Int {
        return when (healthStatus) {
            HEALTH_NORMAL -> android.R.color.holo_green_dark
            HEALTH_SICK -> android.R.color.holo_red_dark
            HEALTH_INJURED -> android.R.color.holo_orange_dark
            HEALTH_PREGNANT -> android.R.color.holo_blue_dark
            HEALTH_DEAD -> android.R.color.darker_gray
            else -> android.R.color.black
        }
    }
    
    /**
     * Firebase에서 사용할 수 있는 Map 형태로 변환
     */
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "earTag" to earTag,
            "species" to species,
            "breed" to breed,
            "gender" to gender,
            "birthDate" to birthDate,
            "weight" to weight,
            "mother" to mother,
            "father" to father,
            "location" to location,
            "healthStatus" to healthStatus,
            "note" to note,
            "photoUrls" to photoUrls,
            "ownerId" to ownerId,
            "ownerName" to ownerName,
            "registrationDate" to registrationDate,
            "isActive" to isActive,
            "createdAt" to createdAt,
            "updatedAt" to System.currentTimeMillis()
        )
    }
    
    /**
     * 유효성 검사
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (earTag.isBlank()) errors.add("귀표번호를 입력해주세요")
        if (species.isBlank()) errors.add("축종을 선택해주세요")
        if (gender.isBlank()) errors.add("성별을 선택해주세요")
        if (birthDate.isBlank()) errors.add("출생일을 입력해주세요")
        if (ownerName.isBlank()) errors.add("소유자명을 입력해주세요")
        
        return errors
    }
}