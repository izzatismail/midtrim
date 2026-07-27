package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.repository.EntitlementCacheWriter
import com.izzatismail.midtrim.domain.repository.PlayBillingService
import com.izzatismail.midtrim.domain.repository.PurchaseResult
import com.izzatismail.midtrim.domain.repository.RestoreResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseEntitlementUseCaseTest {
    @Test
    fun `successful purchase updates cache`() = runBlocking {
        val store = FakePlayBillingService(result = PurchaseResult.Success)
        val cache = FakeEntitlementCache()
        val useCase = PurchaseEntitlementUseCase(store, cache)
        val result = useCase.execute()
        assertEquals(PurchaseResult.Success, result)
        assertTrue(cache.isPurchased)
        assertTrue(cache.didWrite)
    }

    @Test
    fun `cancellation surfaces non-error`() = runBlocking {
        val store = FakePlayBillingService(result = PurchaseResult.Cancelled)
        val cache = FakeEntitlementCache()
        val useCase = PurchaseEntitlementUseCase(store, cache)
        val result = useCase.execute()
        assertEquals(PurchaseResult.Cancelled, result)
        assertFalse(cache.isPurchased)
    }

    @Test
    fun `payment failure surfaces retryable error`() = runBlocking {
        val store = FakePlayBillingService(result = PurchaseResult.Failed("Payment declined"))
        val cache = FakeEntitlementCache()
        val useCase = PurchaseEntitlementUseCase(store, cache)
        val result = useCase.execute()
        assertEquals(PurchaseResult.Failed("Payment declined"), result)
        assertFalse(cache.isPurchased)
    }
}

class RestoreEntitlementUseCaseTest {
    @Test
    fun `restore found updates cache`() = runBlocking {
        val store = FakePlayBillingService(restoreResult = RestoreResult.Found)
        val cache = FakeEntitlementCache()
        val useCase = RestoreEntitlementUseCase(store, cache)
        val result = useCase.execute()
        assertTrue(result)
        assertTrue(cache.isPurchased)
    }

    @Test
    fun `restore not found sets cache to default`() = runBlocking {
        val store = FakePlayBillingService(restoreResult = RestoreResult.NotFound)
        val cache = FakeEntitlementCache()
        val useCase = RestoreEntitlementUseCase(store, cache)
        val result = useCase.execute()
        assertFalse(result)
        assertFalse(cache.isPurchased)
    }

    @Test
    fun `network failure preserves existing cache`() = runBlocking {
        val store = FakePlayBillingService(restoreResult = RestoreResult.Failed("Network error"))
        val cache = FakeEntitlementCache(isPurchased = true)
        val useCase = RestoreEntitlementUseCase(store, cache)
        val result = useCase.execute()
        assertFalse(result)
        assertTrue(cache.isPurchased)
    }

    @Test
    fun `cancelled restore does not clear cache`() = runBlocking {
        val store = FakePlayBillingService(restoreResult = RestoreResult.Failed("Cancelled"))
        val cache = FakeEntitlementCache(isPurchased = true)
        val useCase = RestoreEntitlementUseCase(store, cache)
        val result = useCase.execute()
        assertFalse(result)
        assertTrue(cache.isPurchased)
    }
}

class FakePlayBillingService(
    private val result: PurchaseResult = PurchaseResult.Cancelled,
    private val restoreResult: RestoreResult = RestoreResult.NotFound
) : PlayBillingService {
    override suspend fun purchase(productId: String): PurchaseResult = result
    override suspend fun restorePurchases(): RestoreResult = restoreResult
}

class FakeEntitlementCache(
    isPurchased: Boolean = false,
    productId: String? = null,
    lastVerifiedAt: Long? = null
) : EntitlementCacheWriter {
    override var isPurchased: Boolean = isPurchased
        set(value) {
            field = value
            didWrite = true
        }
    override var productId: String? = productId
    override var lastVerifiedAt: Long? = lastVerifiedAt

    var didWrite = false
        private set
}