import Foundation

struct RestoreEntitlementUseCase {
    private let storeService: StoreKitServiceProtocol
    private let cache: EntitlementCacheProtocol
    private let productID: String

    init(storeService: StoreKitServiceProtocol, cache: EntitlementCacheProtocol, productID: String = "com.midtrim.fullunlock") {
        self.storeService = storeService
        self.cache = cache
        self.productID = productID
    }

    func execute() async -> RestoreResult {
        let result = await storeService.restorePurchases(productID: productID)
        switch result {
        case .found:
            await cache.setPurchased(true)
            await cache.setLastVerified(Date())
        case .notFound:
            await cache.setPurchased(false)
        case .failed:
            break
        }
        return result
    }
}
