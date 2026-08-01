package com.creditjourney.limit.core.domain.valueobject

import com.creditjourney.limit.core.common.messages.CreditLimitMessages.MONEY_MUST_NOT_BE_NEGATIVE
import java.math.BigDecimal

data class Money(
    val value: BigDecimal,
) {
    init {
        require(value >= BigDecimal.ZERO) { MONEY_MUST_NOT_BE_NEGATIVE }
    }

    fun plus(money: Money): Money = Money(value.add(money.value))

    fun minus(money: Money): Money = Money(value.subtract(money.value))

    fun isGreaterThan(money: Money): Boolean = value > money.value

    fun isGreaterThanOrEqualTo(money: Money): Boolean = value >= money.value

    fun isLessThan(money: Money): Boolean = value < money.value

    companion object {
        val ZERO = Money(BigDecimal.ZERO)
    }
}
