package com.accompany.purchaseManagement

enum class PurchaseStatus(
    val displayName: String,
    val emoji: String,
    val color: Int,
    val description: String
) {
    PENDING(
        displayName = "대기중",
        emoji = "⏳",
        color = android.graphics.Color.parseColor("#FF9800"),
        description = "신청이 접수되어 검토 대기중입니다"
    ),

    CONFIRMED(
        displayName = "확인완료",
        emoji = "✅",
        color = android.graphics.Color.parseColor("#2196F3"),
        description = "관리자가 신청을 확인했습니다"
    ),

    IN_APPROVAL(
        displayName = "결재중",
        emoji = "📋",
        color = android.graphics.Color.parseColor("#9C27B0"),
        description = "결재 진행중입니다"
    ),

    APPROVED(
        displayName = "결재완료",
        emoji = "✔️",
        color = android.graphics.Color.parseColor("#4CAF50"),
        description = "결재가 완료되었습니다"
    ),

    PRE_PROCESSED(
        displayName = "선조치",
        emoji = "🚀",
        color = android.graphics.Color.parseColor("#00BCD4"),
        description = "긴급하여 먼저 처리되었습니다"
    ),

    COMPLETED(
        displayName = "완료",
        emoji = "🎉",
        color = android.graphics.Color.parseColor("#4CAF50"),
        description = "구매가 완료되었습니다"
    );

    companion object {
        // 문자열로 상태 찾기
        fun fromString(value: String): PurchaseStatus {
            return values().find { it.displayName == value } ?: PENDING
        }

        // 다음 가능한 상태들 반환
        fun getNextStatuses(current: PurchaseStatus): List<PurchaseStatus> {
            return when (current) {
                PENDING -> listOf(CONFIRMED, PRE_PROCESSED)
                CONFIRMED -> listOf(IN_APPROVAL, PRE_PROCESSED)
                IN_APPROVAL -> listOf(APPROVED, PRE_PROCESSED)
                APPROVED -> listOf(COMPLETED)
                PRE_PROCESSED -> listOf(COMPLETED)
                COMPLETED -> emptyList()
            }
        }

        // 관리자가 변경 가능한 모든 상태
        fun getAllAdminStatuses(): List<PurchaseStatus> {
            return values().toList()
        }
    }
}