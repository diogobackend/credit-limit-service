package com.creditjourney.limit.core.domain.exception

import com.creditjourney.limit.core.common.messages.CreditLimitMessages.CREDIT_LIMIT_NOT_FOUND

class CreditLimitNotFoundException(
    customerId: Long,
) : RuntimeException("$CREDIT_LIMIT_NOT_FOUND for customerId: $customerId")
