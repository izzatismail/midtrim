import XCTest
import StoreKitTest
@testable import MidTrim

final class StoreKitIntegrationTests: XCTestCase {
    private var session: SKTestSession!

    override func setUp() async throws {
        session = try SKTestSession(configurationFileNamed: "MidTrim_StoreKitConfig")
        session.disableDialogs = true
    }

    override func tearDown() async throws {
        session?.clearTransactions()
        session = nil
    }

    func testRestoreNotFoundWithEmptyStoreLeavesCacheAsDefault() async throws {
        let cache = MockEntitlementCache(isPurchased: false)
        let service = StoreKitService()
        let useCase = RestoreEntitlementUseCase(storeService: service, cache: cache)

        let result = await useCase.execute()

        XCTAssertEqual(result, .notFound)
        XCTAssertFalse(cache.isPurchased)
    }
}
