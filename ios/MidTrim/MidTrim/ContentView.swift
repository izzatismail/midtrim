import SwiftUI

struct ContentView: View {
    var body: some View { MidTrimNavigation() }
}

@MainActor
struct MidTrimNavigation: View {
    @State private var showPaywall = false
    @State private var showSettings = false

    private let projectListViewModel: ProjectListViewModel
    private let videoSelectionViewModel: VideoSelectionViewModel

    init() {
        let stubProjectRepo = StubProjectRepository()
        let stubFileRepo = StubVideoFileRepository()
        let stubCache = StubEntitlementCache()
        let stubMetadata = StubVideoMetadataService()

        let fetchProjects = FetchProjectsUseCase(repository: stubProjectRepo)
        let deleteProject = DeleteProjectUseCase(projectRepository: stubProjectRepo, fileRepository: stubFileRepo)
        let renameProject = RenameProjectUseCase(repository: stubProjectRepo)
        let fetchEntitlement = FetchEntitlementStatusUseCase(cache: stubCache)

        projectListViewModel = ProjectListViewModel(
            fetchProjectsUseCase: fetchProjects,
            deleteProjectUseCase: deleteProject,
            renameProjectUseCase: renameProject,
            fetchEntitlementStatusUseCase: fetchEntitlement
        )
        videoSelectionViewModel = VideoSelectionViewModel(
            importVideosUseCase: ImportVideosUseCase(metadataService: stubMetadata, entitlementCache: fetchEntitlement),
            reorderVideosUseCase: ReorderVideosUseCase(),
            calculateMergedDurationUseCase: CalculateMergedDurationUseCase(),
            validateTrimDurationUseCase: ValidateTrimDurationUseCase(),
            fetchEntitlementStatusUseCase: fetchEntitlement
        )
    }

    var body: some View {
        TabView {
            ProjectListScreen(
                viewModel: projectListViewModel,
                onNewProject: { },
                onUpgrade: { showPaywall = true },
                onSettings: { showSettings = true }
            )
            .tabItem { Label("Projects", systemImage: "folder") }
        }
        .sheet(isPresented: $showPaywall) {
            PaywallScreen(onPurchase: { }, onRestore: { }, onDismiss: { showPaywall = false })
        }
        .sheet(isPresented: $showSettings) {
            NavigationStack { HelpSettingsScreen(onRestore: { }, onDismiss: { showSettings = false }) }
        }
    }
}

private struct StubProjectRepository: ProjectRepositoryProtocol {
    func fetchAllProjects() async throws -> [ProjectInfo] { [] }
    func fetchProject(by id: UUID) async throws -> ProjectInfo? { nil }
    func save(project: ProjectInfo, sourceVideos: [SourceVideoInfo]) async throws {}
    func delete(by id: UUID) async throws {}
    func rename(by id: UUID, to name: String) async throws {}
}

private struct StubVideoFileRepository: VideoFileRepositoryProtocol {
    func saveOutputVideo(from sourceUri: String, targetFileName: String) async throws -> String { "" }
    func createDecryptedCopyForShare(uri: String) async throws -> String { "" }
    func createTempSegmentDir() async throws -> String { "" }
    func cleanupTempSegments(dir: String) async throws {}
    func deleteOutputVideo(at uri: String) async throws {}
    func deleteThumbnail(at uri: String) async throws {}
}

private actor StubEntitlementCache: EntitlementCacheProtocol {
    var isPurchased: Bool { false }
    var productId: String? { nil }
    var lastVerifiedAt: Date? { nil }
    func setPurchased(_ purchased: Bool) async {}
    func setProductId(_ id: String?) async {}
    func setLastVerified(_ date: Date?) async {}
}

private struct StubVideoMetadataService: VideoMetadataServiceProtocol {
    func fetchMetadata(for videoURI: String) async throws -> VideoMetadata {
        throw ImportVideoError.metadataFetchFailed("Video processing not available yet")
    }
}
