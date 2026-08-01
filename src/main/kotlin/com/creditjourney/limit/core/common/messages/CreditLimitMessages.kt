package com.creditjourney.limit.core.common.messages

object CreditLimitMessages {
    const val MONEY_MUST_NOT_BE_NEGATIVE = "Money must not be negative"
    const val CUSTOMER_ID_MUST_BE_POSITIVE = "Customer id must be positive"
    const val TOTAL_LIMIT_MUST_BE_POSITIVE = "Total limit must be positive"
    const val AVAILABLE_LIMIT_MUST_NOT_BE_GREATER_THAN_TOTAL_LIMIT = "Available limit must not be greater than total limit"
    const val USED_LIMIT_MUST_NOT_BE_GREATER_THAN_TOTAL_LIMIT = "Used limit must not be greater than total limit"
    const val CREDIT_LIMIT_NOT_FOUND = "Credit limit not found"
}
