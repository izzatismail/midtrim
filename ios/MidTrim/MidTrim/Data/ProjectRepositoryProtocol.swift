import Foundation
import SwiftData

protocol ProjectRepositoryProtocol {
    func fetchAllProjects() async throws -> [Project]
    func fetchProject(by id: PersistentIdentifier) async throws -> Project?
    func save(project: Project) async throws
    func delete(project: Project) async throws
    func rename(project: Project, to name: String) async throws
}
