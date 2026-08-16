package com.izzatismail.midtrim.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PlayBillingServiceImpl(
    context: Context? = null,
    private val activityProvider: () -> Activity? = { null },
    injectedBillingClient: BillingClient? = null
) : PlayBillingService {

    private val productId: String = PRODUCT_ID

    private val connectionReady = CompletableDeferred<Unit>()

    @Volatile
    private var pendingPurchase: CompletableDeferred<PurchaseResult>? = null

    @Volatile
    private var currentProductId: String? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        onPurchasesUpdated(billingResult, purchases)
    }

    private val billingClient: BillingClient = injectedBillingClient
        ?: BillingClient.newBuilder(context!!)
            .setListener(purchasesUpdatedListener)
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
                billingClient.startConnection(this)
            }
        })
    }

    override suspend fun purchase(requestedProductId: String): PurchaseResult {
        return try {
            connectionReady.await()

            val productDetails = resolveProductDetails(requestedProductId)
                ?: return PurchaseResult.Failed("Product not found")

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

            currentProductId = requestedProductId
            val deferred = CompletableDeferred<PurchaseResult>()
            pendingPurchase = deferred

            val billingResult = billingClient.launchBillingFlow(activity, params)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                clearPending()
                return PurchaseResult.Failed("Launch failed: ${billingResult.debugMessage}")
            }

            deferred.await()
        } catch (e: PlayBillingException) {
            PurchaseResult.Failed(e.message ?: "Billing setup failed")
        } catch (e: CancellationException) {
            clearPending()
            throw e
        }
    }

    override suspend fun restorePurchases(): RestoreResult {
        return try {
            connectionReady.await()

            suspendCancellableCoroutine { continuation ->
                billingClient.queryPurchasesAsync(BillingClient.ProductType.INAPP) { billingResult, purchases ->
                    continuation.resume(mapRestoreResult(billingResult, purchases, productId))
                }
            }
        } catch (e: PlayBillingException) {
            RestoreResult.Failed(e.message ?: "Billing setup failed")
        }
    }

    suspend fun resolveProductDetails(requestedProductId: String): ProductDetails? {
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

        return suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, details ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && details.isNotEmpty()) {
                    continuation.resume(details.first())
                } else {
                    continuation.resume(null)
                }
            }
        }
    }

    fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val deferred = pendingPurchase ?: return
        val pid = currentProductId ?: return

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
            val purchaseToAck = purchases.firstOrNull {
                it.purchaseState == Purchase.PurchaseState.PURCHASED && it.products.contains(pid)
            }
            if (purchaseToAck != null) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchaseToAck.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(ackParams) { ackResult ->
                    val finalResult = if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        PurchaseResult.Success
                    } else {
                        PurchaseResult.Failed("Acknowledgment failed: ${ackResult.debugMessage}")
                    }
                    deferred.complete(finalResult)
                    clearPending()
                }
                return
            }
        }

        deferred.complete(mapPurchaseResult(billingResult, purchases, pid))
        clearPending()
    }

    fun endConnection() {
        billingClient.endConnection()
    }

    private fun clearPending() {
        pendingPurchase = null
        currentProductId = null
    }
}

internal const val PRODUCT_ID = "com.midtrim.fullunlock"

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
        val purchased = purchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        if (purchased != null && purchased.products.contains(productId)) {
            return PurchaseResult.Success
        }
        val pending = purchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PENDING }
        if (pending != null && pending.products.contains(productId)) {
            return PurchaseResult.Failed("Purchase is pending. Please complete payment in the store.")
        }
    }

    return when (billingResult.responseCode) {
        BillingClient.BillingResponseCode.USER_CANCELED -> PurchaseResult.Cancelled
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> PurchaseResult.Success
        else -> PurchaseResult.Failed(billingResult.debugMessage)
    }
}

class PlayBillingException(message: String) : Exception(message)
