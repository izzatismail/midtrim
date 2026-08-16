package com.izzatismail.midtrim.data.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.izzatismail.midtrim.domain.repository.PurchaseResult
import com.izzatismail.midtrim.domain.repository.RestoreResult
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class BillingMapperTest {

    private val productId = "com.midtrim.fullunlock"

    @Test
    fun `restore finds purchased product`() {
        val billingResult = result(BillingClient.BillingResponseCode.OK)
        val purchases = listOf(purchaseFor(listOf(productId)))

        val restoreResult = mapRestoreResult(billingResult, purchases, productId)

        assertEquals(RestoreResult.Found, restoreResult)
    }

    @Test
    fun `restore returns not found when no matching product`() {
        val billingResult = result(BillingClient.BillingResponseCode.OK)
        val purchases = listOf(purchaseFor(listOf("com.other.product")))

        val restoreResult = mapRestoreResult(billingResult, purchases, productId)

        assertEquals(RestoreResult.NotFound, restoreResult)
    }

    @Test
    fun `restore returns not found on empty purchase list`() {
        val billingResult = result(BillingClient.BillingResponseCode.OK)

        val restoreResult = mapRestoreResult(billingResult, emptyList(), productId)

        assertEquals(RestoreResult.NotFound, restoreResult)
    }

    @Test
    fun `restore returns not found for pending purchase`() {
        val billingResult = result(BillingClient.BillingResponseCode.OK)
        val purchases = listOf(purchaseFor(listOf(productId), state = JSON_PENDING))

        val restoreResult = mapRestoreResult(billingResult, purchases, productId)

        assertEquals(RestoreResult.NotFound, restoreResult)
    }

    @Test
    fun `restore failed surfaces as failed`() {
        val billingResult = result(BillingClient.BillingResponseCode.NETWORK_ERROR)

        val restoreResult = mapRestoreResult(billingResult, emptyList(), productId)

        assertEquals(RestoreResult.Failed("debug"), restoreResult)
    }

    @Test
    fun `purchase success for matching product`() {
        val billingResult = result(BillingClient.BillingResponseCode.OK)
        val purchases = listOf(purchaseFor(listOf(productId)))

        val purchaseResult = mapPurchaseResult(billingResult, purchases, productId)

        assertEquals(PurchaseResult.Success, purchaseResult)
    }

    @Test
    fun `purchase cancellation surfaces cancelled`() {
        val billingResult = result(BillingClient.BillingResponseCode.USER_CANCELED)

        val purchaseResult = mapPurchaseResult(billingResult, null, productId)

        assertEquals(PurchaseResult.Cancelled, purchaseResult)
    }

    @Test
    fun `purchase failure surfaces failed with message`() {
        val billingResult = result(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE, "Billing unavailable")

        val purchaseResult = mapPurchaseResult(billingResult, null, productId)

        assertEquals(PurchaseResult.Failed("Billing unavailable"), purchaseResult)
    }

    @Test
    fun `refund downgrades cached entitlement to not found without touching products`() {
        val ok = result(BillingClient.BillingResponseCode.OK)
        val purchasedList = listOf(purchaseFor(listOf(productId)))

        val initialRestore = mapRestoreResult(ok, purchasedList, productId)
        assertEquals(RestoreResult.Found, initialRestore)

        val refunded = mapRestoreResult(ok, emptyList(), productId)
        assertEquals(RestoreResult.NotFound, refunded)
    }

    @Test
    fun `already owned purchase returns success without purchases callback`() {
        val billingResult = result(BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED)

        val purchaseResult = mapPurchaseResult(billingResult, null, productId)

        assertEquals(PurchaseResult.Success, purchaseResult)
    }

    private fun result(code: Int, msg: String = "debug"): BillingResult {
        return BillingResult.newBuilder()
            .setResponseCode(code)
            .setDebugMessage(msg)
            .build()
    }

    private fun purchaseFor(products: List<String>, state: Int = JSON_PURCHASED): Purchase {
        val json = JSONObject()
            .put("productIds", JSONArray(products))
            .put("purchaseState", state)
            .put("purchaseTime", System.currentTimeMillis())
        return Purchase(json.toString(), "signature")
    }

    private companion object {
        const val JSON_PURCHASED = 1
        const val JSON_PENDING = 4
    }
}