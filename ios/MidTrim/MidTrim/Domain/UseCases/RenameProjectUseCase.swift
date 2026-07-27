import Foundation

struct RenameProjectUseCase {
    private let repository: ProjectRepositoryProtocol

    init(repository: ProjectRepositoryProtocol) {
        self.repository = repository
    }

    func execute(projectID: UUID, newName: String) async throws {
        let trimmed = newName.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else {
            throw ProjectError.emptyName
        }
        try await repository.rename(by: projectID, to: trimmed)
    }
}
