import Foundation

struct PurchaseEntitlementUseCase {
    private let storeService: StoreKitServiceProtocol
    private let cache: EntitlementCacheProtocol
    private let productID: String

    init(storeService: StoreKitServiceProtocol, cache: EntitlementCacheProtocol, productID: String = "com.midtrim.fullunlock") {
        self.storeService = storeService
        self.cache = cache
        self.productID = productID
    }

    func execute() async -> PurchaseResult {
        let result = await storeService.purchase(productID: productID)
        if case .success = result {
            await cache.setPurchased(true)
            await cache.setProductId(productID)
            await cache.setLastVerified(Date())
        }
        return result
    }
}
