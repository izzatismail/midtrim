import XCTest
@testable import MidTrim

final class SaveProjectUseCaseTests: XCTestCase {
    func testSaveSuccess() async throws {
        let repo = MockProjectRepository()
        let useCase = SaveProjectUseCase(repository: repo)
        let project = ProjectInfo(
            id: UUID(), name: "Test", trimDuration: 3, wasCustomDuration: false,
            outputVideoURI: "out.mp4", thumbnailURI: "thumb.jpg", exportQualityTier: "free_720p",
            mergedDuration: 3, videoCount: 1, createdAt: Date(), updatedAt: Date()
        )
        try await useCase.execute(project: project, sourceVideos: [])
    }

    func testSaveWithEmptyNameThrows() async {
        let repo = MockProjectRepository()
        let useCase = SaveProjectUseCase(repository: repo)
        let project = ProjectInfo(
            id: UUID(), name: "", trimDuration: 3, wasCustomDuration: false,
            outputVideoURI: "out.mp4", thumbnailURI: "thumb.jpg", exportQualityTier: "free_720p",
            mergedDuration: 3, videoCount: 1, createdAt: Date(), updatedAt: Date()
        )
        do {
            try await useCase.execute(project: project, sourceVideos: [])
            XCTFail("Expected emptyName error")
        } catch let error as ProjectError {
            XCTAssertEqual(error, .emptyName)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testSaveWithWhitespaceNameThrows() async {
        let repo = MockProjectRepository()
        let useCase = SaveProjectUseCase(repository: repo)
        let project = ProjectInfo(
            id: UUID(), name: "   ", trimDuration: 3, wasCustomDuration: false,
            outputVideoURI: "out.mp4", thumbnailURI: "thumb.jpg", exportQualityTier: "free_720p",
            mergedDuration: 3, videoCount: 1, createdAt: Date(), updatedAt: Date()
        )
        do {
            try await useCase.execute(project: project, sourceVideos: [])
            XCTFail("Expected emptyName error")
        } catch let error as ProjectError {
            XCTAssertEqual(error, .emptyName)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }
}
