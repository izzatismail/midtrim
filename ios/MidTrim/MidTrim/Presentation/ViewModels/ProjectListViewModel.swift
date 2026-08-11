import SwiftUI
import Combine

@MainActor
class ProjectListViewModel: ObservableObject {
    @Published var uiState = ProjectListUiState()

    private let fetchProjectsUseCase: FetchProjectsUseCase
    private let deleteProjectUseCase: DeleteProjectUseCase
    private let renameProjectUseCase: RenameProjectUseCase
    private let fetchEntitlementStatusUseCase: FetchEntitlementStatusUseCase

    init(
        fetchProjectsUseCase: FetchProjectsUseCase,
        deleteProjectUseCase: DeleteProjectUseCase,
        renameProjectUseCase: RenameProjectUseCase,
        fetchEntitlementStatusUseCase: FetchEntitlementStatusUseCase
    ) {
        self.fetchProjectsUseCase = fetchProjectsUseCase
        self.deleteProjectUseCase = deleteProjectUseCase
        self.renameProjectUseCase = renameProjectUseCase
        self.fetchEntitlementStatusUseCase = fetchEntitlementStatusUseCase
    }

    func loadProjects() {
        Task { @MainActor in
            uiState.isLoading = true
            do {
                let projects = try await fetchProjectsUseCase.execute()
                let isPaid = await fetchEntitlementStatusUseCase.isPaidUser
                uiState = ProjectListUiState(
                    projects: projects,
                    isLoading: false,
                    isPaidUser: isPaid
                )
            } catch {
                uiState.isLoading = false
                uiState.error = error.localizedDescription
            }
        }
    }

    func deleteProject(_ project: ProjectInfo) {
        Task { @MainActor in
            do {
                try await deleteProjectUseCase.execute(project: project)
                loadProjects()
            } catch {
                uiState.error = error.localizedDescription
            }
        }
    }

    func renameProject(id: UUID, name: String) {
        Task { @MainActor in
            do {
                try await renameProjectUseCase.execute(projectID: id, newName: name)
                loadProjects()
            } catch {
                uiState.error = error.localizedDescription
            }
        }
    }
}

struct ProjectListUiState {
    var projects: [ProjectInfo] = []
    var isLoading = true
    var isPaidUser = false
    var error: String?
}
