package com.izzatismail.midtrim.domain.repository

interface PlayBillingService {
    suspend fun purchase(productId: String): PurchaseResult
    suspend fun restorePurchases(): RestoreResult
}