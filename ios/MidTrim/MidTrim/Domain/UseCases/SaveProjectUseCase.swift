import Foundation

struct SaveProjectUseCase {
    private let repository: ProjectRepositoryProtocol

    init(repository: ProjectRepositoryProtocol) {
        self.repository = repository
    }

    func execute(project: ProjectInfo, sourceVideos: [SourceVideoInfo]) async throws {
        guard !project.name.trimmingCharacters(in: .whitespaces).isEmpty else {
            throw ProjectError.emptyName
        }
        let trimmed = ProjectInfo(
            id: project.id, name: project.name.trimmingCharacters(in: .whitespaces),
            trimDuration: project.trimDuration, wasCustomDuration: project.wasCustomDuration,
            outputVideoURI: project.outputVideoURI, thumbnailURI: project.thumbnailURI,
            exportQualityTier: project.exportQualityTier, mergedDuration: project.mergedDuration,
            videoCount: project.videoCount, createdAt: project.createdAt, updatedAt: project.updatedAt
        )
        try await repository.save(project: trimmed, sourceVideos: sourceVideos)
    }
}
