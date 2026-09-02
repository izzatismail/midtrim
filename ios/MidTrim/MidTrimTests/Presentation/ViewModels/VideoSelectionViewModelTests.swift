import XCTest
@testable import MidTrim

final class VideoSelectionViewModelTests: XCTestCase {
    private var viewModel: VideoSelectionViewModel!

    override func setUp() {
        super.setUp()
        viewModel = VideoSelectionViewModel(
            importVideosUseCase: ImportVideosUseCase(
                metadataService: FakeVideoMetadataService(),
                entitlementCache: FakeEntitlementCacheMulti(isPurchased: true)
            ),
            reorderVideosUseCase: ReorderVideosUseCase(),
            calculateMergedDurationUseCase: CalculateMergedDurationUseCase(),
            validateTrimDurationUseCase: ValidateTrimDurationUseCase(),
            fetchEntitlementStatusUseCase: FetchEntitlementStatusUseCase(cache: FakeEntitlementCacheMulti(isPurchased: true))
        )
        viewModel.initialize()
    }

    func testSelectCustomModeSetsCustomFlagsForPaidUser() {
        viewModel.selectCustomMode()
        XCTAssertTrue(viewModel.uiState.isCustomDurationSelected)
        XCTAssertTrue(viewModel.uiState.wasCustomDuration)
        XCTAssertEqual(viewModel.uiState.trimDuration, VideoSelectionViewModel.defaultCustomDuration, accuracy: 0.001)
        XCTAssertEqual(viewModel.uiState.customDurationValue, VideoSelectionViewModel.defaultCustomDuration, accuracy: 0.001)
    }

    func testSetCustomDurationClampsAtMinimum() {
        viewModel.selectCustomMode()
        viewModel.setCustomDuration(0.5)
        XCTAssertEqual(viewModel.uiState.trimDuration, VideoSelectionViewModel.customDurationMin, accuracy: 0.001)
        XCTAssertTrue(viewModel.uiState.wasCustomDuration)
    }

    func testSetCustomDurationClampsAtMaximum() {
        viewModel.selectCustomMode()
        viewModel.setCustomDuration(5.5)
        XCTAssertEqual(viewModel.uiState.trimDuration, VideoSelectionViewModel.customDurationMax, accuracy: 0.001)
        XCTAssertTrue(viewModel.uiState.wasCustomDuration)
    }

    func testSetCustomDurationRoundsToNearestStep() {
        viewModel.selectCustomMode()
        viewModel.setCustomDuration(2.35)
        XCTAssertEqual(viewModel.uiState.trimDuration, 2.4, accuracy: 0.001)
    }

    func testSetTrimDurationResetsCustomFlags() {
        viewModel.selectCustomMode()
        viewModel.setTrimDuration(2.0)
        XCTAssertEqual(viewModel.uiState.trimDuration, 2.0, accuracy: 0.001)
        XCTAssertFalse(viewModel.uiState.isCustomDurationSelected)
        XCTAssertFalse(viewModel.uiState.wasCustomDuration)
    }

    func testSelectCustomModeRecalculatesMergedDuration() {
        viewModel.setTrimDuration(2.0)
        viewModel.selectCustomMode()
        let expected = CalculateMergedDurationUseCase().execute(
            trimDuration: VideoSelectionViewModel.defaultCustomDuration,
            videoCount: viewModel.uiState.selectedVideos.count
        )
        XCTAssertEqual(viewModel.uiState.mergedDuration, expected, accuracy: 0.001)
    }

    func testSetCustomDurationUpdatesMergedDuration() {
        viewModel.selectCustomMode()
        viewModel.setCustomDuration(4.5)
        let expected = CalculateMergedDurationUseCase().execute(
            trimDuration: 4.5,
            videoCount: viewModel.uiState.selectedVideos.count
        )
        XCTAssertEqual(viewModel.uiState.mergedDuration, expected, accuracy: 0.001)
    }
}

/// Minimal entitlement cache supporting both reader and writer protocols for test.
private class FakeEntitlementCacheMulti: EntitlementCacheWriter {
    var isPurchased: Bool = false
    var productId: String?
    var lastVerifiedAt: Int64?

    init(isPurchased: Bool) {
        self.isPurchased = isPurchased
    }
}

/// Stub metadata service returning placeholder values.
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
