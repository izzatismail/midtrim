import XCTest
@testable import MidTrim

final class KeychainEntitlementCacheTests: XCTestCase {
    private var cache: KeychainEntitlementCache!

    override func setUp() async throws {
        cache = KeychainEntitlementCache()
        // Reset to default state
        await cache.setPurchased(false)
        await cache.setProductId(nil)
        await cache.setLastVerified(nil)
    }

    func testDefaultStateIsNotPurchased() async {
        let purchased = await cache.isPurchased
        let productId = await cache.productId
        let lastVerified = await cache.lastVerifiedAt
        XCTAssertFalse(purchased)
        XCTAssertNil(productId)
        XCTAssertNil(lastVerified)
    }

    func testWriteAndReadPurchasedState() async {
        await cache.setPurchased(true)
        let purchased = await cache.isPurchased
        XCTAssertTrue(purchased)
    }

    func testWriteAndReadProductId() async {
        await cache.setProductId("com.midtrim.fullunlock")
        let productId = await cache.productId
        XCTAssertEqual(productId, "com.midtrim.fullunlock")
    }

    func testWriteAndReadLastVerified() async {
        let date = Date(timeIntervalSince1970: 1000)
        await cache.setLastVerified(date)
        let lastVerified = await cache.lastVerifiedAt
        XCTAssertEqual(lastVerified?.timeIntervalSince1970, 1000, accuracy: 0.001)
    }

    func testEntitlementCachePersistsAcrossInstances() async {
        await cache.setPurchased(true)
        await cache.setProductId("com.midtrim.fullunlock")
        await cache.setLastVerified(Date(timeIntervalSince1970: 1000))

        let freshCache = KeychainEntitlementCache()
        let purchased = await freshCache.isPurchased
        let productId = await freshCache.productId
        let lastVerified = await freshCache.lastVerifiedAt

        XCTAssertTrue(purchased)
        XCTAssertEqual(productId, "com.midtrim.fullunlock")
        XCTAssertEqual(lastVerified?.timeIntervalSince1970, 1000, accuracy: 0.001)
    }
}
