import Foundation

protocol VideoFileRepositoryProtocol {
    func saveOutputVideo(from sourceUri: String, targetFileName: String) async throws -> String
    func createDecryptedCopyForShare(uri: String) async throws -> String
    func createTempSegmentDir() async throws -> String
    func cleanupTempSegments(dir: String) async throws
    func deleteOutputVideo(at uri: String) async throws
    func deleteThumbnail(at uri: String) async throws
}
