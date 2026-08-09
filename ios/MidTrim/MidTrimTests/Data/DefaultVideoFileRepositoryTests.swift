import XCTest
@testable import MidTrim

final class DefaultVideoFileRepositoryTests: XCTestCase {
    private var repository: DefaultVideoFileRepository!
    private let fileManager = FileManager.default

    override func setUp() {
        super.setUp()
        repository = DefaultVideoFileRepository(fileManager: fileManager)
    }

    func testSaveOutputVideoCreatesFileWithProtection() async throws {
        let tempDir = fileManager.temporaryDirectory
        let sourceURL = tempDir.appendingPathComponent("test_source_\(UUID().uuidString).mp4")
        try "fake video content".write(to: sourceURL, atomically: true, encoding: .utf8)

        let path = try await repository.saveOutputVideo(
            from: sourceURL.path,
            targetFileName: "test_encrypted.mp4"
        )
        let savedURL = URL(fileURLWithPath: path)
        XCTAssertTrue(fileManager.fileExists(atPath: path))

        let attributes = try fileManager.attributesOfItem(atPath: path)
        let protection = attributes[.protectionKey] as? FileProtectionType
        XCTAssertEqual(protection, FileProtectionType.complete)

        try fileManager.removeItem(at: savedURL)
        try fileManager.removeItem(at: sourceURL)
    }

    func testCreateTempSegmentDirCreatesDirectory() async throws {
        let dir = try await repository.createTempSegmentDir()
        var isDir: ObjCBool = false
        XCTAssertTrue(fileManager.fileExists(atPath: dir, isDirectory: &isDir))
        XCTAssertTrue(isDir.boolValue)
        try fileManager.removeItem(atPath: dir)
    }

    func testCleanupTempSegmentsRemovesDirectory() async throws {
        let dir = try await repository.createTempSegmentDir()
        try await repository.cleanupTempSegments(dir: dir)
        XCTAssertFalse(fileManager.fileExists(atPath: dir))
    }

    func testDeleteOutputVideoRemovesFile() async throws {
        let tempURL = fileManager.temporaryDirectory.appendingPathComponent("\(UUID().uuidString).mp4")
        try "test".write(to: tempURL, atomically: true, encoding: .utf8)

        try await repository.deleteOutputVideo(at: tempURL.path)
        XCTAssertFalse(fileManager.fileExists(atPath: tempURL.path))
    }

    func testDeleteThumbnailRemovesFile() async throws {
        let tempURL = fileManager.temporaryDirectory.appendingPathComponent("\(UUID().uuidString).jpg")
        try "test".write(to: tempURL, atomically: true, encoding: .utf8)

        try await repository.deleteThumbnail(at: tempURL.path)
        XCTAssertFalse(fileManager.fileExists(atPath: tempURL.path))
    }
}
