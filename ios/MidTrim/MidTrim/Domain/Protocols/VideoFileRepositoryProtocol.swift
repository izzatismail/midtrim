import Foundation

protocol VideoFileRepositoryProtocol {
    func deleteOutputVideo(at uri: String) async throws
    func deleteThumbnail(at uri: String) async throws
}
