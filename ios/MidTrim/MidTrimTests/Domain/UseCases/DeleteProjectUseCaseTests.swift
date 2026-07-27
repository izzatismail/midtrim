import XCTest
@testable import MidTrim

final class DeleteProjectUseCaseTests: XCTestCase {
    func testDeleteSuccess() async throws {
        let fileRepo = MockVideoFileRepository()
        let projectRepo = MockProjectRepository()
        let useCase = DeleteProjectUseCase(projectRepository: projectRepo, fileRepository: fileRepo)
        let project = ProjectInfo(
            id: UUID(), name: "Test", trimDuration: 3, wasCustomDuration: false,
            outputVideoURI: "out.mp4", thumbnailURI: "thumb.jpg", exportQualityTier: "free_720p",
            mergedDuration: 3, videoCount: 1, createdAt: Date(), updatedAt: Date()
        )
        try await useCase.execute(project: project)
        XCTAssertTrue(fileRepo.didDeleteVideo)
        XCTAssertTrue(fileRepo.didDeleteThumbnail)
    }

    func testDeleteFileNotFoundContinuesCascade() async {
        let fileRepo = MockVideoFileRepository(shouldThrow: true)
        let projectRepo = MockProjectRepository()
        let useCase = DeleteProjectUseCase(projectRepository: projectRepo, fileRepository: fileRepo)
        let project = ProjectInfo(
            id: UUID(), name: "Test", trimDuration: 3, wasCustomDuration: false,
            outputVideoURI: "out.mp4", thumbnailURI: "thumb.jpg", exportQualityTier: "free_720p",
            mergedDuration: 3, videoCount: 1, createdAt: Date(), updatedAt: Date()
        )
        try? await useCase.execute(project: project)
        XCTAssertTrue(fileRepo.didDeleteVideo)
        XCTAssertTrue(fileRepo.didDeleteThumbnail)
        XCTAssertEqual(projectRepo.lastDeletedID, project.id)
    }
}

final class MockVideoFileRepository: VideoFileRepositoryProtocol {
    private let shouldThrow: Bool
    private(set) var didDeleteVideo = false
    private(set) var didDeleteThumbnail = false

    init(shouldThrow: Bool = false) {
        self.shouldThrow = shouldThrow
    }

    func deleteOutputVideo(at uri: String) async throws {
        didDeleteVideo = true
        if shouldThrow { throw NSError(domain: "test", code: 1) }
    }

    func deleteThumbnail(at uri: String) async throws {
        didDeleteThumbnail = true
        if shouldThrow { throw NSError(domain: "test", code: 1) }
    }
}

final class MockProjectRepository: ProjectRepositoryProtocol {
    private(set) var lastDeletedID: UUID?
    private let projects: [ProjectInfo]

    init(projects: [ProjectInfo]? = nil) {
        if let projects {
            self.projects = projects
        } else {
            let a = ProjectInfo(
                id: UUID(), name: "Project A", trimDuration: 2, wasCustomDuration: false,
                outputVideoURI: "a.mp4", thumbnailURI: "a.jpg", exportQualityTier: "free_720p",
                mergedDuration: 4, videoCount: 2,
                createdAt: Date().addingTimeInterval(-3600), updatedAt: Date().addingTimeInterval(-3600)
            )
            let b = ProjectInfo(
                id: UUID(), name: "Project B", trimDuration: 3, wasCustomDuration: false,
                outputVideoURI: "b.mp4", thumbnailURI: "b.jpg", exportQualityTier: "free_720p",
                mergedDuration: 3, videoCount: 1,
                createdAt: Date(), updatedAt: Date()
            )
            self.projects = [b, a]
        }
    }

    func fetchAllProjects() async throws -> [ProjectInfo] { projects }
    func fetchProject(by id: UUID) async throws -> ProjectInfo? { projects.first { $0.id == id } }
    func save(project: ProjectInfo, sourceVideos: [SourceVideoInfo]) async throws {}
    func delete(by id: UUID) async throws { lastDeletedID = id }
    func rename(by id: UUID, to name: String) async throws {}
}
