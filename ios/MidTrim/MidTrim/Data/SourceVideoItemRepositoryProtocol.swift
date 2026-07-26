import Foundation
import SwiftData

protocol SourceVideoItemRepositoryProtocol {
    func fetchSourceVideoItems(for projectId: PersistentIdentifier) async throws -> [SourceVideoItem]
    func saveSourceVideoItems(_ items: [SourceVideoItem]) async throws
    func deleteSourceVideoItems(for projectId: PersistentIdentifier) async throws
}
