package com.creditjourney.limit.core.domain.model

import com.creditjourney.limit.core.common.messages.CreditLimitMessages.AVAILABLE_LIMIT_MUST_NOT_BE_GREATER_THAN_TOTAL_LIMIT
import com.creditjourney.limit.core.common.messages.CreditLimitMessages.CUSTOMER_ID_MUST_BE_POSITIVE
import com.creditjourney.limit.core.common.messages.CreditLimitMessages.TOTAL_LIMIT_MUST_BE_POSITIVE
import com.creditjourney.limit.core.common.messages.CreditLimitMessages.USED_LIMIT_MUST_NOT_BE_GREATER_THAN_TOTAL_LIMIT
import com.creditjourney.limit.core.domain.valueobject.Money
import java.time.LocalDateTime
import java.util.UUID

data class CreditLimit(
    val limitId: UUID = UUID.randomUUID(),
    val customerId: Long,
    val totalLimit: Money,
    val availableLimit: Money,
    val usedLimit: Money = Money.ZERO,
    val status: LimitStatus = LimitStatus.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    init {
        require(customerId > 0) { CUSTOMER_ID_MUST_BE_POSITIVE }
        require(totalLimit.isGreaterThan(Money.ZERO)) { TOTAL_LIMIT_MUST_BE_POSITIVE }
        require(!availableLimit.isGreaterThan(totalLimit)) {
            AVAILABLE_LIMIT_MUST_NOT_BE_GREATER_THAN_TOTAL_LIMIT
        }
        require(!usedLimit.isGreaterThan(totalLimit)) {
            USED_LIMIT_MUST_NOT_BE_GREATER_THAN_TOTAL_LIMIT
        }
    }

    fun block(): CreditLimit =
        copy(
            status = LimitStatus.BLOCKED,
            updatedAt = LocalDateTime.now(),
        )

    fun release(): CreditLimit =
        copy(
            status = LimitStatus.ACTIVE,
            updatedAt = LocalDateTime.now(),
        )

    fun cancel(): CreditLimit =
        copy(
            status = LimitStatus.CANCELLED,
            updatedAt = LocalDateTime.now(),
        )
}
