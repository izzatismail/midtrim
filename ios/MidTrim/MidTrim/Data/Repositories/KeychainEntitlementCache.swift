import Foundation
import Security

actor KeychainEntitlementCache: EntitlementCacheProtocol {
    private let service = "com.midtrim.entitlement"
    private let account = "entitlement_singleton"

    var isPurchased: Bool {
        get async { (try? readValue(for: .isPurchased) as Bool) ?? false }
    }

    var productId: String? {
        get async { try? readValue(for: .productId) as String }
    }

    var lastVerifiedAt: Date? {
        get async {
            guard let timestamp: TimeInterval = try? readValue(for: .lastVerifiedAt) else { return nil }
            return Date(timeIntervalSince1970: timestamp)
        }
    }

    func setPurchased(_ purchased: Bool) async {
        try? writeValue(purchased, for: .isPurchased)
    }

    func setProductId(_ id: String?) async {
        if let id {
            try? writeValue(id, for: .productId)
        } else {
            try? deleteValue(for: .productId)
        }
    }

    func setLastVerified(_ date: Date?) async {
        if let date {
            try? writeValue(date.timeIntervalSince1970, for: .lastVerifiedAt)
        } else {
            try? deleteValue(for: .lastVerifiedAt)
        }
    }

    private enum Key: String, CaseIterable {
        case isPurchased
        case productId
        case lastVerifiedAt
    }

    private func readValue<T: Codable>(for key: Key) throws -> T {
        var query = baseQuery()
        query[kSecReturnData as String] = kCFBooleanTrue
        query[kSecMatchLimit as String] = kSecMatchLimitOne
        query[kSecAttrAccount as String] = "\(account).\(key.rawValue)"

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)

        guard status == errSecSuccess, let data = item as? Data else {
            throw KeychainError.readFailed(status: status)
        }

        return try JSONDecoder().decode(T.self, from: data)
    }

    private func writeValue<T: Codable>(_ value: T, for key: Key) throws {
        let data = try JSONEncoder().encode(value)
        var query = baseQuery()
        query[kSecAttrAccount as String] = "\(account).\(key.rawValue)"

        SecItemDelete(query as CFDictionary)

        query[kSecValueData as String] = data
        let status = SecItemAdd(query as CFDictionary, nil)
        guard status == errSecSuccess else {
            throw KeychainError.writeFailed(status: status)
        }
    }

    private func deleteValue(for key: Key) throws {
        var query = baseQuery()
        query[kSecAttrAccount as String] = "\(account).\(key.rawValue)"
        let status = SecItemDelete(query as CFDictionary)
        guard status == errSecSuccess || status == errSecItemNotFound else {
            throw KeychainError.deleteFailed(status: status)
        }
    }

    private func baseQuery() -> [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        ]
    }

    enum KeychainError: Error {
        case readFailed(status: OSStatus)
        case writeFailed(status: OSStatus)
        case deleteFailed(status: OSStatus)
    }
}
