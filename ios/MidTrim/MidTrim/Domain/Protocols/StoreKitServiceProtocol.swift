import Foundation

enum PurchaseResult: Equatable {
    case success
    case cancelled
    case failed(String)
}

enum RestoreResult: Equatable {
    case found
    case notFound
    case failed(String)
}

protocol StoreKitServiceProtocol {
    func purchase(productID: String) async -> PurchaseResult
    func restorePurchases(productID: String) async -> RestoreResult
}
