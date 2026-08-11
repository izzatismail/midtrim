import Foundation

struct FetchEntitlementStatusUseCase: @unchecked Sendable {
    private let cache: EntitlementCacheProtocol

    init(cache: EntitlementCacheProtocol) {
        self.cache = cache
    }

    var isPaidUser: Bool {
        get async { await cache.isPurchased }
    }
}
