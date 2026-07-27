import XCTest
@testable import MidTrim

final class FetchEntitlementStatusUseCaseTests: XCTestCase {
    func testReturnsCachedTrue() async {
        let cache = MockEntitlementCache(isPurchased: true)
        let useCase = FetchEntitlementStatusUseCase(cache: cache)
        let result = await useCase.isPaidUser
        XCTAssertTrue(result)
    }

    func testReturnsCachedFalse() async {
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = FetchEntitlementStatusUseCase(cache: cache)
        let result = await useCase.isPaidUser
        XCTAssertFalse(result)
    }

    func testDoesNotTriggerLiveStoreQuery() async {
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = FetchEntitlementStatusUseCase(cache: cache)
        _ = await useCase.isPaidUser
        XCTAssertFalse(cache.didWrite)
    }
}

final class MockEntitlementCache: EntitlementCacheProtocol {
    var isPurchased: Bool
    let productId: String?
    let lastVerifiedAt: Date?
    private(set) var didWrite = false

    init(isPurchased: Bool, productId: String? = nil, lastVerifiedAt: Date? = nil) {
        self.isPurchased = isPurchased
        self.productId = productId
        self.lastVerifiedAt = lastVerifiedAt
    }

    func setPurchased(_ purchased: Bool) {
        isPurchased = purchased
        didWrite = true
    }

    func setProductId(_ id: String?) {}
    func setLastVerified(_ date: Date?) {}
}
