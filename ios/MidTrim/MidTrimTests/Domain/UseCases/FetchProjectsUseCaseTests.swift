import XCTest
@testable import MidTrim

final class FetchProjectsUseCaseTests: XCTestCase {
    func testReturnsAllProjects() async throws {
        let repo = MockProjectRepository()
        let useCase = FetchProjectsUseCase(repository: repo)
        let result = try await useCase.execute()
        XCTAssertEqual(result.count, 2)
    }

    func testEmptyDatabase() async throws {
        let repo = MockProjectRepository(projects: [])
        let useCase = FetchProjectsUseCase(repository: repo)
        let result = try await useCase.execute()
        XCTAssertTrue(result.isEmpty)
    }

    func testOrdering() async throws {
        let repo = MockProjectRepository()
        let useCase = FetchProjectsUseCase(repository: repo)
        let result = try await useCase.execute()
        XCTAssertEqual(result.map(\.name), ["Project B", "Project A"])
    }
}
