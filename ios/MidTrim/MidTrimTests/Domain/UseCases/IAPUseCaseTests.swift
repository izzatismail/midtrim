import XCTest
@testable import MidTrim

final class PurchaseEntitlementUseCaseTests: XCTestCase {
    func testSuccessfulPurchaseUpdatesCache() async {
        let store = MockStoreKitService(result: .success)
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = PurchaseEntitlementUseCase(storeService: store, cache: cache)
        let result = await useCase.execute()
        XCTAssertEqual(result, .success)
        XCTAssertTrue(cache.isPurchased)
        XCTAssertTrue(cache.didWrite)
    }

    func testCancellationSurfacesNonError() async {
        let store = MockStoreKitService(result: .cancelled)
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = PurchaseEntitlementUseCase(storeService: store, cache: cache)
        let result = await useCase.execute()
        XCTAssertEqual(result, .cancelled)
        XCTAssertFalse(cache.isPurchased)
    }

    func testPaymentFailureSurfacesRetryableError() async {
        let store = MockStoreKitService(result: .failed("Payment declined"))
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = PurchaseEntitlementUseCase(storeService: store, cache: cache)
        let result = await useCase.execute()
        guard case .failed(let message) = result else {
            return XCTFail("Expected failed")
        }
        XCTAssertEqual(message, "Payment declined")
        XCTAssertFalse(cache.isPurchased)
    }
}

final class RestoreEntitlementUseCaseTests: XCTestCase {
    func testRestoreFoundUpdatesCache() async {
        let store = MockStoreKitService(restoreResult: .found)
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = RestoreEntitlementUseCase(storeService: store, cache: cache)
        let result = await useCase.execute()
        XCTAssertEqual(result, .found)
        XCTAssertTrue(cache.isPurchased)
    }

    func testRestoreNotFoundLeavesCacheAsDefault() async {
        let store = MockStoreKitService(restoreResult: .notFound)
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = RestoreEntitlementUseCase(storeService: store, cache: cache)
        let result = await useCase.execute()
        XCTAssertEqual(result, .notFound)
        XCTAssertFalse(cache.isPurchased)
    }

    func testNetworkFailurePreservesExistingCache() async {
        let store = MockStoreKitService(restoreResult: .failed("Network error"))
        let cache = MockEntitlementCache(isPurchased: true)
        let useCase = RestoreEntitlementUseCase(storeService: store, cache: cache)
        let result = await useCase.execute()
        guard case .failed = result else {
            return XCTFail("Expected failed")
        }
        XCTAssertTrue(cache.isPurchased)
    }
}

final class MockStoreKitService: StoreKitServiceProtocol {
    private let purchaseResult: PurchaseResult
    private let restoreResult: RestoreResult

    init(result: PurchaseResult, restoreResult: RestoreResult = .notFound) {
        self.purchaseResult = result
        self.restoreResult = restoreResult
    }

    init(restoreResult: RestoreResult) {
        self.purchaseResult = .cancelled
        self.restoreResult = restoreResult
    }

    func purchase(productID: String) async -> PurchaseResult {
        purchaseResult
    }

    func restorePurchases(productID: String) async -> RestoreResult {
        restoreResult
    }
}
