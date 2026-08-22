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
    @State private var paywallIsLoading = false
    @State private var paywallError: String?
    @State private var paywallPrice = "$5.00"
    @State private var settingsRestoreResult: String?

    private let projectListViewModel: ProjectListViewModel
    private let videoSelectionViewModel: VideoSelectionViewModel
    private let storeService: StoreKitService
    private let cache: KeychainEntitlementCache

    init(modelContainer: ModelContainer) {
        let projectRepo = SwiftDataProjectRepository(modelContainer: modelContainer)
        let fileRepo = DefaultVideoFileRepository()
        let cache = KeychainEntitlementCache()
        let metadataService = FakeVideoMetadataService()
        let storeService = StoreKitService()

        self.storeService = storeService
        self.cache = cache

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
            PaywallScreen(
                price: paywallPrice,
                onPurchase: {
                    paywallError = nil
                    paywallIsLoading = true
                    Task { @MainActor in
                        let purchase = PurchaseEntitlementUseCase(
                            storeService: storeService,
                            cache: cache
                        )
                        let result = await purchase.execute()
                        paywallIsLoading = false
                        switch result {
                        case .success:
                            triggerHaptic(.success)
                            showPaywall = false
                            projectListViewModel.loadProjects()
                            videoSelectionViewModel.initialize()
                        case .cancelled:
                            paywallError = "Purchase cancelled"
                        case .failed(let msg):
                            paywallError = msg
                        }
                    }
                },
                onRestore: {
                    paywallError = nil
                    paywallIsLoading = true
                    Task { @MainActor in
                        let restore = RestoreEntitlementUseCase(
                            storeService: storeService,
                            cache: cache
                        )
                        let result = await restore.execute()
                        paywallIsLoading = false
                        switch result {
                        case .found:
                            triggerHaptic(.success)
                            showPaywall = false
                            projectListViewModel.loadProjects()
                            videoSelectionViewModel.initialize()
                        case .notFound:
                            paywallError = "No purchases found to restore"
                        case .failed:
                            paywallError = "Restore failed. Please try again."
                        }
                    }
                },
                onDismiss: { showPaywall = false },
                isLoading: paywallIsLoading,
                error: paywallError
            )
        }
        .sheet(isPresented: $showSettings) {
            NavigationStack {
                HelpSettingsScreen(
                    onRestore: {
                        settingsRestoreResult = nil
                        Task { @MainActor in
                            let restore = RestoreEntitlementUseCase(
                                storeService: storeService,
                                cache: cache
                            )
                            let result = await restore.execute()
                            switch result {
                            case .found:
                                triggerHaptic(.success)
                                settingsRestoreResult = "Purchases restored"
                            case .notFound:
                                settingsRestoreResult = "No purchases found to restore"
                            case .failed:
                                settingsRestoreResult = "Restore failed. Please try again."
                            }
                        }
                    },
                    onPrivacyPolicy: {
                        if let url = URL(string: "https://www.example.com/privacy") {
                            UIApplication.shared.open(url)
                        }
                    },
                    onLicenses: {
                        if let url = URL(string: "https://scripts.sil.org/OFL") {
                            UIApplication.shared.open(url)
                        }
                    },
                    onDismiss: { showSettings = false },
                    restoreResult: settingsRestoreResult
                )
            }
        }
        .task {
            async let priceTask = storeService.fetchPrice()
            async let restoreTask = RestoreEntitlementUseCase(storeService: storeService, cache: cache).execute()

            if let price = await priceTask {
                paywallPrice = price
            }
            let restoreResult = await restoreTask
            if restoreResult == .found {
                projectListViewModel.loadProjects()
                videoSelectionViewModel.initialize()
            }
        }
    }

    private func triggerHaptic(_ style: UINotificationFeedbackGenerator.FeedbackType) {
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(style)
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
