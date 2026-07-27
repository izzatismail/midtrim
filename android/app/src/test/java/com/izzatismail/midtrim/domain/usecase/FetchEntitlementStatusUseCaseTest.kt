package com.izzatismail.midtrim.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchEntitlementStatusUseCaseTest {
    @Test
    fun `returns cached true`() {
        val cache = FakeEntitlementCache(isPurchased = true)
        val useCase = FetchEntitlementStatusUseCase(cache)
        assertTrue(useCase.isPaidUser)
    }

    @Test
    fun `returns cached false`() {
        val cache = FakeEntitlementCache(isPurchased = false)
        val useCase = FetchEntitlementStatusUseCase(cache)
        assertFalse(useCase.isPaidUser)
    }

    @Test
    fun `does not trigger live store query`() {
        val cache = FakeEntitlementCache(isPurchased = false)
        val useCase = FetchEntitlementStatusUseCase(cache)
        useCase.isPaidUser
        assertFalse(cache.didWrite)
    }
}