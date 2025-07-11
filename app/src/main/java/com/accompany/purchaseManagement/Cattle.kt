package com.accompany.purchaseManagement // ← 패키지명 맞게 수정

data class Cattle(
    val managementNumber: String, // 관리번호
    val id: String,               // 개체번호
    val birthDate: String,        // 생년월일
    val ageMonths: Int,           // 월령
    val category: String,         // 개체구분
    val sex: String,              // 성별
    val weight: Float?,           // 체중 (nullable)
    val barn: String,             // 축사
    val status: String?           // 상태 (nullable)
)
