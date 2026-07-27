import Foundation

struct FetchProjectsUseCase {
    private let repository: ProjectRepositoryProtocol

    init(repository: ProjectRepositoryProtocol) {
        self.repository = repository
    }

    func execute() async throws -> [ProjectInfo] {
        try await repository.fetchAllProjects()
    }
}
