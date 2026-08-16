package com.izzatismail.midtrim.data.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.GetBillingConfigParams
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageResponseListener
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryResponseListener
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.izzatismail.midtrim.domain.repository.PurchaseResult
import com.izzatismail.midtrim.domain.repository.RestoreResult
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayBillingServiceImplTest {

    private val productId = "com.midtrim.fullunlock"

    @Test
    fun `restore finds purchased product end to end`() = runBlocking {
        val purchases = listOf(purchase(listOf(productId)))
        val client = FakeBillingClient(
            restorePurchases = { listener -> listener.onQueryPurchasesResponse(billingOk(), purchases) }
        )
        val service = PlayBillingServiceImpl(injectedBillingClient = client)

        val result = service.restorePurchases()

        assertEquals(RestoreResult.Found, result)
    }

    @Test
    fun `restore not found end to end`() = runBlocking {
        val client = FakeBillingClient(
            restorePurchases = { listener -> listener.onQueryPurchasesResponse(billingOk(), emptyList()) }
        )
        val service = PlayBillingServiceImpl(injectedBillingClient = client)

        val result = service.restorePurchases()

        assertEquals(RestoreResult.NotFound, result)
    }

    @Test
    fun `restore network failure end to end`() = runBlocking {
        val client = FakeBillingClient(
            restorePurchases = { listener ->
                listener.onQueryPurchasesResponse(
                    BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.NETWORK_ERROR).setDebugMessage("Network").build(),
                    emptyList()
                )
            }
        )
        val service = PlayBillingServiceImpl(injectedBillingClient = client)

        val result = service.restorePurchases()

        assertEquals(RestoreResult.Failed("Network"), result)
    }

    @Test
    fun `connection failure surfaces purchase failed`() = runBlocking {
        val errorResult = BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.ERROR).build()
        val client = FakeBillingClient(onStartConnection = errorResult)
        val service = PlayBillingServiceImpl(injectedBillingClient = client)

        val result = service.purchase(productId)

        assertEquals(PurchaseResult.Failed("Setup failed: "), result)
    }

    private fun billingOk(): BillingResult {
        return BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()
    }

    private fun purchase(products: List<String>): Purchase {
        val json = JSONObject()
            .put("productIds", JSONArray(products))
            .put("purchaseState", 1)
            .put("purchaseTime", System.currentTimeMillis())
        return Purchase(json.toString(), "sig")
    }
}

class FakeBillingClient(
    private val onStartConnection: BillingResult? = null,
    private val restorePurchases: ((PurchasesResponseListener) -> Unit)? = null
) : BillingClient() {

    override fun getConnectionState(): Int = BillingClient.ConnectionState.CONNECTED
    override fun isReady(): Boolean = true
    override fun endConnection() {}

    override fun startConnection(listener: BillingClientStateListener) {
        if (onStartConnection != null) {
            listener.onBillingSetupFinished(onStartConnection)
        } else {
            listener.onBillingSetupFinished(
                BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()
            )
        }
    }

    override fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult {
        return BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build()
    }

    override fun queryPurchasesAsync(productType: String, listener: PurchasesResponseListener) {
        if (restorePurchases != null) {
            restorePurchases(listener)
        } else {
            listener.onQueryPurchasesResponse(
                BillingResult.newBuilder().setResponseCode(BillingClient.BillingResponseCode.OK).build(),
                emptyList()
            )
        }
    }

    override fun acknowledgePurchase(params: AcknowledgePurchaseParams, listener: AcknowledgePurchaseResponseListener) {}

    override fun queryProductDetailsAsync(params: QueryProductDetailsParams, listener: ProductDetailsResponseListener) {}
    override fun isFeatureSupported(feature: String): BillingResult = BillingResult.newBuilder().build()
    override fun querySkuDetailsAsync(params: SkuDetailsParams, listener: SkuDetailsResponseListener) {}
    override fun queryPurchaseHistoryAsync(type: String, listener: PurchaseHistoryResponseListener) {}
    override fun queryPurchaseHistoryAsync(params: QueryPurchaseHistoryParams, listener: PurchaseHistoryResponseListener) {}
    override fun queryPurchasesAsync(params: QueryPurchasesParams, listener: PurchasesResponseListener) {}
    override fun consumeAsync(params: ConsumeParams, listener: ConsumeResponseListener) {}
    override fun getBillingConfigAsync(params: GetBillingConfigParams, listener: com.android.billingclient.api.BillingConfigResponseListener) {}
    override fun isAlternativeBillingOnlyAvailableAsync(listener: com.android.billingclient.api.AlternativeBillingOnlyAvailabilityListener) {}
    override fun isExternalOfferAvailableAsync(listener: com.android.billingclient.api.ExternalOfferAvailabilityListener) {}
    override fun createAlternativeBillingOnlyReportingDetailsAsync(listener: com.android.billingclient.api.AlternativeBillingOnlyReportingDetailsListener) {}
    override fun createExternalOfferReportingDetailsAsync(listener: com.android.billingclient.api.ExternalOfferReportingDetailsListener) {}
    override fun showAlternativeBillingOnlyInformationDialog(activity: Activity, listener: com.android.billingclient.api.AlternativeBillingOnlyInformationDialogListener): BillingResult {
        return BillingResult.newBuilder().build()
    }
    override fun showExternalOfferInformationDialog(activity: Activity, listener: com.android.billingclient.api.ExternalOfferInformationDialogListener): BillingResult {
        return BillingResult.newBuilder().build()
    }
    override fun showInAppMessages(activity: Activity, params: InAppMessageParams, listener: InAppMessageResponseListener): BillingResult {
        return BillingResult.newBuilder().build()
    }
}