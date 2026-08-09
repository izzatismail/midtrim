import Foundation

final class DefaultVideoFileRepository: VideoFileRepositoryProtocol {
    private let fileManager: FileManager

    init(fileManager: FileManager = .default) {
        self.fileManager = fileManager
    }

    func saveOutputVideo(from sourceUri: String, targetFileName: String) async throws -> String {
        guard let documentsDir = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first else {
            throw VideoFileRepositoryError.missingDirectory
        }
        let outputURL = documentsDir.appendingPathComponent(targetFileName)

        let sourceURL = URL(fileURLWithPath: sourceUri)
        try fileManager.copyItem(at: sourceURL, to: outputURL)

        let attributes: [FileAttributeKey: Any] = [
            .protectionKey: FileProtectionType.complete
        ]
        try fileManager.setAttributes(attributes, ofItemAtPath: outputURL.path)

        return outputURL.path
    }

    func createDecryptedCopyForShare(uri: String) async throws -> String {
        guard let cacheDir = fileManager.urls(for: .cachesDirectory, in: .userDomainMask).first else {
            throw VideoFileRepositoryError.missingDirectory
        }
        let sourceURL = URL(fileURLWithPath: uri)
        let shareURL = cacheDir.appendingPathComponent(
            "share_\(sourceURL.deletingPathExtension().lastPathComponent)_\(Date().timeIntervalSince1970).mp4"
        )

        try fileManager.copyItem(at: sourceURL, to: shareURL)
        return shareURL.path
    }

    func createTempSegmentDir() async throws -> String {
        guard let cacheDir = fileManager.urls(for: .cachesDirectory, in: .userDomainMask).first else {
            throw VideoFileRepositoryError.missingDirectory
        }
        let tempDir = cacheDir.appendingPathComponent("trim_segments_\(UUID().uuidString)")
        try fileManager.createDirectory(at: tempDir, withIntermediateDirectories: true)
        return tempDir.path
    }

    func cleanupTempSegments(dir: String) async throws {
        let url = URL(fileURLWithPath: dir)
        if fileManager.fileExists(atPath: dir) {
            try fileManager.removeItem(at: url)
        }
    }

    func deleteOutputVideo(at uri: String) async throws {
        let url = URL(fileURLWithPath: uri)
        if fileManager.fileExists(atPath: uri) {
            try fileManager.removeItem(at: url)
        }
    }

    func deleteThumbnail(at uri: String) async throws {
        let url = URL(fileURLWithPath: uri)
        if fileManager.fileExists(atPath: uri) {
            try fileManager.removeItem(at: url)
        }
    }
}

enum VideoFileRepositoryError: Error {
    case missingDirectory
}
