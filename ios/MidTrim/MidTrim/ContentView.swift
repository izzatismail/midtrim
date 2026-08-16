import SwiftUI
import SwiftData

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    var body: some View { MidTrimNavigation(modelContainer: modelContext.container) }
}

private enum AppRoute: Hashable {
    case projectList
    case videoSelection
    case trimDuration
    case nameProject
}

@MainActor
struct MidTrimNavigation: View {
    @State private var path = [AppRoute]()
    @State private var showPaywall = false
    @State private var showSettings = false

    private let projectListViewModel: ProjectListViewModel
    private let videoSelectionViewModel: VideoSelectionViewModel

    init(modelContainer: ModelContainer) {
        let projectRepo = SwiftDataProjectRepository(modelContainer: modelContainer)
        let fileRepo = DefaultVideoFileRepository()
        let cache = KeychainEntitlementCache()
        let metadataService = FakeVideoMetadataService()
        let storeService = FakeStoreKitService()

        let fetchProjects = FetchProjectsUseCase(repository: projectRepo)
        let deleteProject = DeleteProjectUseCase(projectRepository: projectRepo, fileRepository: fileRepo)
        let renameProject = RenameProjectUseCase(repository: projectRepo)
        let fetchEntitlement = FetchEntitlementStatusUseCase(cache: cache)

        projectListViewModel = ProjectListViewModel(
            fetchProjectsUseCase: fetchProjects,
            deleteProjectUseCase: deleteProject,
            renameProjectUseCase: renameProject,
            fetchEntitlementStatusUseCase: fetchEntitlement
        )
        videoSelectionViewModel = VideoSelectionViewModel(
            importVideosUseCase: ImportVideosUseCase(metadataService: metadataService, entitlementCache: fetchEntitlement),
            reorderVideosUseCase: ReorderVideosUseCase(),
            calculateMergedDurationUseCase: CalculateMergedDurationUseCase(),
            validateTrimDurationUseCase: ValidateTrimDurationUseCase(),
            fetchEntitlementStatusUseCase: fetchEntitlement
        )

        Task {
            let restore = RestoreEntitlementUseCase(storeService: storeService, cache: cache)
            _ = await restore.execute()
        }
    }

    var body: some View {
        NavigationStack(path: $path) {
            ProjectListScreen(
                viewModel: projectListViewModel,
                onNewProject: { path.append(.videoSelection) },
                onUpgrade: { showPaywall = true },
                onSettings: { showSettings = true }
            )
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .projectList:
                    ProjectListScreen(
                        viewModel: projectListViewModel,
                        onNewProject: { path.append(.videoSelection) },
                        onUpgrade: { showPaywall = true },
                        onSettings: { showSettings = true }
                    )
                case .videoSelection:
                    VideoSelectionScreen(
                        viewModel: videoSelectionViewModel,
                        onContinue: { path.append(.trimDuration) },
                        onBack: { path.removeLast() },
                        onUpgrade: { showPaywall = true }
                    )
                case .trimDuration:
                    let isPaid = videoSelectionViewModel.uiState.isPaidUser
                    TrimDurationScreen(
                        selectedDuration: videoSelectionViewModel.uiState.trimDuration,
                        isPaidUser: isPaid,
                        onDurationSelected: { videoSelectionViewModel.setTrimDuration($0) },
                        onCustomTap: {
                            if isPaid {
                                videoSelectionViewModel.setTrimDuration(4.0)
                            } else {
                                showPaywall = true
                            }
                        },
                        onContinue: { path.append(.nameProject) },
                        onBack: { path.removeLast() }
                    )
                case .nameProject:
                    NameProjectScreen(
                        defaultName: "Trim Project",
                        onSave: { _ in path.removeAll() },
                        onDiscard: { path.removeAll() }
                    )
                }
            }
        }
        .sheet(isPresented: $showPaywall) {
            PaywallScreen(onPurchase: { }, onRestore: { }, onDismiss: { showPaywall = false })
        }
        .sheet(isPresented: $showSettings) {
            NavigationStack { HelpSettingsScreen(onRestore: { }, onDismiss: { showSettings = false }) }
        }
    }
}

private struct FakeVideoMetadataService: VideoMetadataServiceProtocol {
    func fetchMetadata(for videoURI: String) async throws -> VideoMetadata {
        VideoMetadata(
            uri: videoURI,
            duration: 10.0,
            resolution: CGSize(width: 1920, height: 1080),
            fileSize: 1_000_000,
            format: "mp4"
        )
    }
}

private actor FakeStoreKitService: StoreKitServiceProtocol {
    func purchase(productID: String) async -> PurchaseResult { .cancelled }
    func restorePurchases() async -> RestoreResult { .notFound }
}
