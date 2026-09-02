import SwiftUI
import Combine

@MainActor
class VideoSelectionViewModel: ObservableObject {
    static let defaultCustomDuration: Double = 3.0
    static let customDurationMin: Double = 1.0
    static let customDurationMax: Double = 5.0
    static let customDurationStep: Double = 0.1

    @Published var uiState = VideoSelectionUiState()

    private let importVideosUseCase: ImportVideosUseCase
    private let reorderVideosUseCase: ReorderVideosUseCase
    private let calculateMergedDurationUseCase: CalculateMergedDurationUseCase
    private let validateTrimDurationUseCase: ValidateTrimDurationUseCase
    private let fetchEntitlementStatusUseCase: FetchEntitlementStatusUseCase

    init(
        importVideosUseCase: ImportVideosUseCase,
        reorderVideosUseCase: ReorderVideosUseCase,
        calculateMergedDurationUseCase: CalculateMergedDurationUseCase,
        validateTrimDurationUseCase: ValidateTrimDurationUseCase,
        fetchEntitlementStatusUseCase: FetchEntitlementStatusUseCase
    ) {
        self.importVideosUseCase = importVideosUseCase
        self.reorderVideosUseCase = reorderVideosUseCase
        self.calculateMergedDurationUseCase = calculateMergedDurationUseCase
        self.validateTrimDurationUseCase = validateTrimDurationUseCase
        self.fetchEntitlementStatusUseCase = fetchEntitlementStatusUseCase
    }

    func initialize() {
        Task { @MainActor in
            uiState.isPaidUser = await fetchEntitlementStatusUseCase.isPaidUser
            let videos = uiState.selectedVideos
            let sourceWidth = videos.map { Int($0.resolution.width) }.max() ?? 1920
            let sourceHeight = videos.map { Int($0.resolution.height) }.max() ?? 1080
            uiState.sourceWidth = sourceWidth
            uiState.sourceHeight = sourceHeight
            uiState.availableQualityPresets = ExportQualityPreset.availableFor(sourceHeight: sourceHeight)
        }
    }

    func importVideos(_ uris: [String]) {
        Task { @MainActor in
            uiState.isLoading = true
            do {
                let metadata = try await importVideosUseCase.execute(videoURIs: uris)
                let existingURIs = Set(uiState.selectedVideos.map(\.uri))
                let newMetadata = metadata.filter { !existingURIs.contains($0.uri) }
                uiState.selectedVideos.append(contentsOf: newMetadata)
                uiState.mergedDuration = calculateMergedDurationUseCase.execute(
                    trimDuration: uiState.trimDuration,
                    videoCount: uiState.selectedVideos.count
                )
                let sourceWidth = uiState.selectedVideos.map { Int($0.resolution.width) }.max() ?? 1920
                let sourceHeight = uiState.selectedVideos.map { Int($0.resolution.height) }.max() ?? 1080
                uiState.sourceWidth = sourceWidth
                uiState.sourceHeight = sourceHeight
                uiState.availableQualityPresets = ExportQualityPreset.availableFor(sourceHeight: sourceHeight)
                uiState.isLoading = false
            } catch {
                uiState.isLoading = false
                uiState.importError = error.localizedDescription
            }
        }
    }

    func removeVideo(at index: Int) {
        guard uiState.selectedVideos.indices.contains(index) else { return }
        uiState.selectedVideos.remove(at: index)
        uiState.mergedDuration = calculateMergedDurationUseCase.execute(
            trimDuration: uiState.trimDuration,
            videoCount: uiState.selectedVideos.count
        )
    }

    func reorder(from source: IndexSet, to destination: Int) {
        uiState.selectedVideos.move(fromOffsets: source, toOffset: destination)
    }

    func setTrimDuration(_ duration: Double) {
        guard validateTrimDurationUseCase.isAllowed(
            trimDuration: duration,
            isPaidUser: uiState.isPaidUser
        ) else { return }
        uiState.trimDuration = duration
        uiState.mergedDuration = calculateMergedDurationUseCase.execute(
            trimDuration: duration,
            videoCount: uiState.selectedVideos.count
        )
        uiState.isCustomDurationSelected = false
        uiState.wasCustomDuration = false
    }

    func selectCustomMode() {
        let value = Self.defaultCustomDuration
        guard validateTrimDurationUseCase.isAllowed(
            trimDuration: value,
            isPaidUser: uiState.isPaidUser
        ) else { return }
        let merged = calculateMergedDurationUseCase.execute(
            trimDuration: value,
            videoCount: uiState.selectedVideos.count
        )
        uiState.trimDuration = value
        uiState.mergedDuration = merged
        uiState.isCustomDurationSelected = true
        uiState.customDurationValue = value
        uiState.wasCustomDuration = true
    }

    func setCustomDuration(_ value: Double) {
        let clamped = min(max(value, Self.customDurationMin), Self.customDurationMax)
        let stepped = (clamped / Self.customDurationStep).rounded() * Self.customDurationStep
        guard validateTrimDurationUseCase.isAllowed(
            trimDuration: stepped,
            isPaidUser: uiState.isPaidUser
        ) else { return }
        let merged = calculateMergedDurationUseCase.execute(
            trimDuration: stepped,
            videoCount: uiState.selectedVideos.count
        )
        uiState.trimDuration = stepped
        uiState.mergedDuration = merged
        uiState.customDurationValue = stepped
        uiState.wasCustomDuration = true
    }

    func setQualityPreset(_ preset: ExportQualityPreset) {
        uiState.selectedQualityPreset = preset
    }
}

struct VideoSelectionUiState {
    var selectedVideos: [VideoMetadata] = []
    var trimDuration: Double = 1.0
    var mergedDuration: Double = 0.0
    var isPaidUser = false
    var isLoading = false
    var importError: String?
    var selectedQualityPreset: ExportQualityPreset = .best
    var availableQualityPresets: [ExportQualityPreset] = []
    var sourceWidth: Int = 1920
    var sourceHeight: Int = 1080
    var isCustomDurationSelected = false
    var customDurationValue: Double = VideoSelectionViewModel.defaultCustomDuration
    var wasCustomDuration = false
}
