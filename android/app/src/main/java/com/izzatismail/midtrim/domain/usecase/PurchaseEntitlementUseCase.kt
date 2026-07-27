package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.repository.EntitlementCacheWriter
import com.izzatismail.midtrim.domain.repository.PlayBillingService
import com.izzatismail.midtrim.domain.repository.PurchaseResult
import java.util.Date

class PurchaseEntitlementUseCase(
    private val billingService: PlayBillingService,
    private val cache: EntitlementCacheWriter
) {
    private val productId = "com.midtrim.fullunlock"

    suspend fun execute(): PurchaseResult {
        val result = billingService.purchase(productId)
        if (result is PurchaseResult.Success) {
            cache.isPurchased = true
            cache.productId = productId
            cache.lastVerifiedAt = Date().time
        }
        return result
    }
}