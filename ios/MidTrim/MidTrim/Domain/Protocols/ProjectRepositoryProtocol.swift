import Foundation

protocol ProjectRepositoryProtocol {
    func fetchAllProjects() async throws -> [ProjectInfo]
    func fetchProject(by id: UUID) async throws -> ProjectInfo?
    func save(project: ProjectInfo, sourceVideos: [SourceVideoInfo]) async throws
    func delete(by id: UUID) async throws
    func rename(by id: UUID, to name: String) async throws
}
