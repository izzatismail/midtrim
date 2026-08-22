import StoreKit

actor StoreKitService: StoreKitServiceProtocol {
    private let productID: String

    init(productID: String = "com.midtrim.fullunlock") {
        self.productID = productID
    }

    func purchase(productID: String) async -> PurchaseResult {
        guard let product = try? await Product.products(for: [productID]).first else {
            return .failed("Product not found")
        }

        let result: Product.PurchaseResult
        do {
            result = try await product.purchase()
        } catch {
            return .failed(error.localizedDescription)
        }

        switch result {
        case .success(let verification):
            switch verification {
            case .verified(let transaction):
                await transaction.finish()
                return .success
            case .unverified:
                return .failed("Purchase verification failed")
            }
        case .userCancelled:
            return .cancelled
        case .pending:
            return .failed("Purchase is pending. Please complete payment in the store.")
        @unknown default:
            return .failed("Unknown purchase result")
        }
    }

    func restorePurchases() async -> RestoreResult {
        var found = false
        for await verification in Transaction.currentEntitlements {
            switch verification {
            case .verified(let transaction):
                if transaction.productID == productID && transaction.revocationDate == nil {
                    found = true
                }
            case .unverified:
                continue
            }
        }
        return found ? .found : .notFound
    }

    func fetchProductPrice(productID: String) async -> String? {
        guard let product = try? await Product.products(for: [productID]).first else {
            return nil
        }
        return product.displayPrice
    }
}
