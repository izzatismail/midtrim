import Foundation
import SwiftData

final class SwiftDataProjectRepository: ProjectRepositoryProtocol {
    private let modelContainer: ModelContainer
    private let modelContext: ModelContext

    init(modelContainer: ModelContainer) {
        self.modelContainer = modelContainer
        self.modelContext = ModelContext(modelContainer)
    }

    func fetchAllProjects() async throws -> [ProjectInfo] {
        let descriptor = FetchDescriptor<Project>(
            sortBy: [SortDescriptor(\.createdAt, order: .reverse)]
        )
        let projects = try modelContext.fetch(descriptor)
        return projects.map { $0.toDomain() }
    }

    func fetchProject(by id: UUID) async throws -> ProjectInfo? {
        let predicate = #Predicate<Project> { $0.id == id }
        let descriptor = FetchDescriptor<Project>(predicate: predicate)
        let projects = try modelContext.fetch(descriptor)
        return projects.first?.toDomain()
    }

    func save(project: ProjectInfo, sourceVideos: [SourceVideoInfo]) async throws {
        let model = project.toModel()
        modelContext.insert(model)
        for videoInfo in sourceVideos {
            let videoModel = videoInfo.toModel()
            videoModel.project = model
            modelContext.insert(videoModel)
        }
        try modelContext.save()
    }

    func delete(by id: UUID) async throws {
        let predicate = #Predicate<Project> { $0.id == id }
        let descriptor = FetchDescriptor<Project>(predicate: predicate)
        guard let project = try modelContext.fetch(descriptor).first else { return }
        modelContext.delete(project)
        try modelContext.save()
    }

    func rename(by id: UUID, to name: String) async throws {
        let predicate = #Predicate<Project> { $0.id == id }
        let descriptor = FetchDescriptor<Project>(predicate: predicate)
        guard let project = try modelContext.fetch(descriptor).first else { return }
        project.name = name
        try modelContext.save()
    }
}
