import Foundation

protocol EntitlementCacheProtocol {
    var isPurchased: Bool { get async }
    var productId: String? { get async }
    var lastVerifiedAt: Date? { get async }
    func setPurchased(_ purchased: Bool) async
    func setProductId(_ id: String?) async
    func setLastVerified(_ date: Date?) async
}
