import Foundation

struct RestoreEntitlementUseCase {
    private let storeService: StoreKitServiceProtocol
    private let cache: EntitlementCacheProtocol

    init(storeService: StoreKitServiceProtocol, cache: EntitlementCacheProtocol) {
        self.storeService = storeService
        self.cache = cache
    }

    func execute() async -> RestoreResult {
        let result = await storeService.restorePurchases()
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
