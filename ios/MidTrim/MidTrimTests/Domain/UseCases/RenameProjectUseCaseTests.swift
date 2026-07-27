import XCTest
@testable import MidTrim

final class RenameProjectUseCaseTests: XCTestCase {
    func testRenameSuccess() async throws {
        let repo = MockProjectRepository()
        let useCase = RenameProjectUseCase(repository: repo)
        try await useCase.execute(projectID: UUID(), newName: "Updated")
    }

    func testRenameEmptyNameThrows() async {
        let repo = MockProjectRepository()
        let useCase = RenameProjectUseCase(repository: repo)
        do {
            try await useCase.execute(projectID: UUID(), newName: "")
            XCTFail("Expected emptyName error")
        } catch let error as ProjectError {
            XCTAssertEqual(error, .emptyName)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testRenameWhitespaceNameThrows() async {
        let repo = MockProjectRepository()
        let useCase = RenameProjectUseCase(repository: repo)
        do {
            try await useCase.execute(projectID: UUID(), newName: "  ")
            XCTFail("Expected emptyName error")
        } catch let error as ProjectError {
            XCTAssertEqual(error, .emptyName)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }
}
