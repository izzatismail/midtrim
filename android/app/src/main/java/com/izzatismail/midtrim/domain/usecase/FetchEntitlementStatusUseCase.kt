package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.repository.EntitlementCacheReader

class FetchEntitlementStatusUseCase(
    private val cache: EntitlementCacheReader
) {
    val isPaidUser: Boolean get() = cache.isPurchased
}