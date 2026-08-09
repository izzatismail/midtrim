import XCTest
@testable import MidTrim
import SwiftData

final class SwiftDataProjectRepositoryTests: XCTestCase {
    private var modelContainer: ModelContainer!
    private var repository: SwiftDataProjectRepository!

    override func setUpWithError() throws {
        let config = ModelConfiguration(isStoredInMemoryOnly: true)
        modelContainer = try ModelContainer(
            for: Project.self, SourceVideoItem.self,
            configurations: config
        )
        repository = SwiftDataProjectRepository(modelContainer: modelContainer)
    }

    func testInsertAndFetchAllProjects() async throws {
        try await repository.save(
            project: makeProject(name: "Project A"),
            sourceVideos: [makeVideo(id: UUID(), projectId: UUID())]
        )
        try await repository.save(
            project: makeProject(name: "Project B"),
            sourceVideos: [makeVideo(id: UUID(), projectId: UUID())]
        )

        let projects = try await repository.fetchAllProjects()
        XCTAssertEqual(projects.count, 2)
    }

    func testFetchProjectById() async throws {
        let project = makeProject(name: "Test")
        try await repository.save(project: project, sourceVideos: [])

        let fetched = try await repository.fetchProject(by: project.id)
        XCTAssertNotNil(fetched)
        XCTAssertEqual(fetched?.name, "Test")
    }

    func testFetchNonExistentProjectReturnsNil() async throws {
        let result = try await repository.fetchProject(by: UUID())
        XCTAssertNil(result)
    }

    func testDeleteProject() async throws {
        let project = makeProject(name: "To Delete")
        try await repository.save(project: project, sourceVideos: [])
        XCTAssertEqual(try await repository.fetchAllProjects().count, 1)

        try await repository.delete(by: project.id)
        XCTAssertEqual(try await repository.fetchAllProjects().count, 0)
    }

    func testCascadeDeleteRemovesSourceVideos() async throws {
        let project = makeProject(name: "Cascade")
        let video1 = makeVideo(id: UUID(), projectId: project.id)
        let video2 = makeVideo(id: UUID(), projectId: project.id)
        try await repository.save(project: project, sourceVideos: [video1, video2])

        try await repository.delete(by: project.id)
        let allProjects = try await repository.fetchAllProjects()
        XCTAssertTrue(allProjects.isEmpty)
    }

    func testRenameProject() async throws {
        let project = makeProject(name: "Original")
        try await repository.save(project: project, sourceVideos: [])

        try await repository.rename(by: project.id, to: "Renamed")
        let fetched = try await repository.fetchProject(by: project.id)
        XCTAssertEqual(fetched?.name, "Renamed")
    }

    func testOrderingIsCreatedAtDescending() async throws {
        let projectA = ProjectInfo(
            id: UUID(), name: "A",
            trimDuration: 3.0, wasCustomDuration: false,
            outputVideoURI: "A.mp4", thumbnailURI: "A_thumb.jpg",
            exportQualityTier: "free_720p", mergedDuration: 3.0,
            videoCount: 1, createdAt: Date(timeIntervalSince1970: 1000),
            updatedAt: Date()
        )
        let projectB = ProjectInfo(
            id: UUID(), name: "B",
            trimDuration: 3.0, wasCustomDuration: false,
            outputVideoURI: "B.mp4", thumbnailURI: "B_thumb.jpg",
            exportQualityTier: "free_720p", mergedDuration: 3.0,
            videoCount: 1, createdAt: Date(timeIntervalSince1970: 2000),
            updatedAt: Date()
        )
        let projectC = ProjectInfo(
            id: UUID(), name: "C",
            trimDuration: 3.0, wasCustomDuration: false,
            outputVideoURI: "C.mp4", thumbnailURI: "C_thumb.jpg",
            exportQualityTier: "free_720p", mergedDuration: 3.0,
            videoCount: 1, createdAt: Date(timeIntervalSince1970: 3000),
            updatedAt: Date()
        )

        try await repository.save(project: projectC, sourceVideos: [])
        try await repository.save(project: projectB, sourceVideos: [])
        try await repository.save(project: projectA, sourceVideos: [])

        let projects = try await repository.fetchAllProjects()
        XCTAssertEqual(projects.map(\.name), ["C", "B", "A"])
    }

    // MARK: - Helpers

    private func makeProject(name: String) -> ProjectInfo {
        ProjectInfo(
            id: UUID(),
            name: name,
            trimDuration: 3.0,
            wasCustomDuration: false,
            outputVideoURI: "\(name).mp4",
            thumbnailURI: "\(name)_thumb.jpg",
            exportQualityTier: "free_720p",
            mergedDuration: 3.0,
            videoCount: 1,
            createdAt: Date(),
            updatedAt: Date()
        )
    }

    private func makeVideo(id: UUID, projectId: UUID) -> SourceVideoInfo {
        SourceVideoInfo(
            id: id,
            projectId: projectId,
            sourceVideoURI: "source.mp4",
            sourceVideoDuration: 10.0,
            sourceFileSize: 1024,
            orderIndex: 0,
            trimStartTime: 0.0,
            trimEndTime: 3.0
        )
    }
}
