import Foundation

protocol SourceVideoItemRepositoryProtocol {
    func fetchSourceVideoItems(for projectId: String) async throws -> [SourceVideoItem]
    func saveSourceVideoItems(_ items: [SourceVideoItem]) async throws
    func deleteSourceVideoItems(for projectId: String) async throws
}
