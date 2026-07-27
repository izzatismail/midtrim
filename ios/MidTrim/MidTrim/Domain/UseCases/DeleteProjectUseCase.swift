import Foundation

struct DeleteProjectUseCase {
    private let projectRepository: ProjectRepositoryProtocol
    private let fileRepository: VideoFileRepositoryProtocol

    init(projectRepository: ProjectRepositoryProtocol, fileRepository: VideoFileRepositoryProtocol) {
        self.projectRepository = projectRepository
        self.fileRepository = fileRepository
    }

    func execute(project: ProjectInfo) async throws {
        try? await fileRepository.deleteOutputVideo(at: project.outputVideoURI)
        try? await fileRepository.deleteThumbnail(at: project.thumbnailURI)
        try await projectRepository.delete(by: project.id)
    }
}
