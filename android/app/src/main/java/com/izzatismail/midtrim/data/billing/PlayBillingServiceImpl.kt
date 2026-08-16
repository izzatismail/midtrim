package com.izzatismail.midtrim.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.izzatismail.midtrim.domain.repository.PlayBillingService
import com.izzatismail.midtrim.domain.repository.PurchaseResult
import com.izzatismail.midtrim.domain.repository.RestoreResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PlayBillingServiceImpl(
    context: Context,
    private val activityProvider: () -> Activity?,
    injectedBillingClient: BillingClient? = null
) : PlayBillingService {

    private val productId = "com.midtrim.fullunlock"

    private val connectionReady = CompletableDeferred<Unit>()

    private var pendingPurchase: CompletableDeferred<PurchaseResult>? = null

    private val billingClient: BillingClient = injectedBillingClient ?: BillingClient.newBuilder(context)
        .setListener { billingResult, purchases -> onPurchasesUpdated(billingResult, purchases) }
        .build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    connectionReady.complete(Unit)
                } else {
                    connectionReady.completeExceptionally(
                        PlayBillingException("Setup failed: ${billingResult.debugMessage}")
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
            }
        })
    }

    override suspend fun purchase(requestedProductId: String): PurchaseResult {
        connectionReady.await()

        val productDetails = resolveProductDetails(requestedProductId) ?: return PurchaseResult.Failed("Product not found")

        val activity = activityProvider() ?: return PurchaseResult.Failed("Activity unavailable")

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        val deferred = CompletableDeferred<PurchaseResult>()
        pendingPurchase = deferred

        val billingResult = billingClient.launchBillingFlow(activity, params)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            pendingPurchase = null
            return PurchaseResult.Failed("Launch failed: ${billingResult.debugMessage}")
        }

        return deferred.await()
    }

    override suspend fun restorePurchases(): RestoreResult {
        connectionReady.await()

        return suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(BillingClient.ProductType.INAPP) { billingResult, purchases ->
                continuation.resume(mapRestoreResult(billingResult, purchases, productId))
            }
        }
    }

    private fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val deferred = pendingPurchase ?: return
        deferred.complete(mapPurchaseResult(billingResult, purchases, productId))
        pendingPurchase = null
    }

    private suspend fun resolveProductDetails(requestedProductId: String): ProductDetails? {
        return suspendCancellableCoroutine { continuation ->
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(requestedProductId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()

            billingClient.queryProductDetailsAsync(params) { billingResult, details ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && details.isNotEmpty()) {
                    continuation.resume(details.first())
                } else {
                    continuation.resume(null)
                }
            }
        }
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}

internal fun mapRestoreResult(
    billingResult: BillingResult,
    purchases: List<Purchase>?,
    productId: String
): RestoreResult {
    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
        return RestoreResult.Failed(billingResult.debugMessage)
    }
    val hasPurchase = purchases.orEmpty().any { p ->
        p.products.contains(productId) && p.purchaseState == Purchase.PurchaseState.PURCHASED
    }
    return if (hasPurchase) RestoreResult.Found else RestoreResult.NotFound
}

internal fun mapPurchaseResult(
    billingResult: BillingResult,
    purchases: List<Purchase>?,
    productId: String
): PurchaseResult {
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
        val purchase = purchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        if (purchase != null && purchase.products.contains(productId)) {
            return PurchaseResult.Success
        }
    }

    return when (billingResult.responseCode) {
        BillingClient.BillingResponseCode.USER_CANCELED -> PurchaseResult.Cancelled
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> PurchaseResult.Success
        else -> PurchaseResult.Failed(billingResult.debugMessage)
    }
}

class PlayBillingException(message: String) : Exception(message)