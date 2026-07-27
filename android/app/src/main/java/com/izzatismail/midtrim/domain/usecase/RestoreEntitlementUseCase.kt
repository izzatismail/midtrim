package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.repository.EntitlementCacheWriter
import com.izzatismail.midtrim.domain.repository.PlayBillingService
import com.izzatismail.midtrim.domain.repository.RestoreResult
import java.util.Date

class RestoreEntitlementUseCase(
    private val billingService: PlayBillingService,
    private val cache: EntitlementCacheWriter
) {
    suspend fun execute(): Boolean {
        val result = billingService.restorePurchases()
        return when (result) {
            is RestoreResult.Found -> {
                cache.isPurchased = true
                cache.lastVerifiedAt = Date().time
                true
            }
            is RestoreResult.NotFound -> {
                cache.isPurchased = false
                false
            }
            is RestoreResult.Failed -> false
        }
    }
}