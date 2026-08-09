package com.izzatismail.midtrim.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.izzatismail.midtrim.data.repository.EncryptedSharedPrefsEntitlementCache
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EntitlementCacheTest {
    private lateinit var cache: EncryptedSharedPrefsEntitlementCache

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        cache = EncryptedSharedPrefsEntitlementCache(context)
        // Reset to default state
        cache.isPurchased = false
        cache.productId = null
        cache.lastVerifiedAt = null
    }

    @Test
    fun `default state is not purchased`() {
        assertFalse(cache.isPurchased)
        assertNull(cache.productId)
        assertNull(cache.lastVerifiedAt)
    }

    @Test
    fun `write and read purchased state`() {
        cache.isPurchased = true
        assertTrue(cache.isPurchased)
    }

    @Test
    fun `write and read product id`() {
        cache.productId = "com.midtrim.fullunlock"
        assertEquals("com.midtrim.fullunlock", cache.productId)
    }

    @Test
    fun `write and read last verified timestamp`() {
        cache.lastVerifiedAt = 1000L
        assertEquals(1000L, cache.lastVerifiedAt)
    }

    @Test
    fun `reset to not purchased`() {
        cache.isPurchased = true
        cache.productId = "com.midtrim.fullunlock"
        cache.lastVerifiedAt = 1000L

        cache.isPurchased = false
        cache.productId = null
        cache.lastVerifiedAt = null

        assertFalse(cache.isPurchased)
        assertNull(cache.productId)
        assertNull(cache.lastVerifiedAt)
    }

    @Test
    fun `entitlement cache persists across instances`() {
        cache.isPurchased = true
        cache.productId = "com.midtrim.fullunlock"
        cache.lastVerifiedAt = 1000L

        val freshCache = EncryptedSharedPrefsEntitlementCache(
            ApplicationProvider.getApplicationContext()
        )
        assertTrue(freshCache.isPurchased)
        assertEquals("com.midtrim.fullunlock", freshCache.productId)
        assertEquals(1000L, freshCache.lastVerifiedAt)
    }
}