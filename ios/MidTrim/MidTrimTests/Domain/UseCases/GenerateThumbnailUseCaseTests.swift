import XCTest
@testable import MidTrim

final class GenerateThumbnailUseCaseTests: XCTestCase {
    func testSuccessfulExtraction() async throws {
        let extractor = MockFrameExtractor(shouldSucceed: true)
        let useCase = GenerateThumbnailUseCase(frameExtractor: extractor)
        let result = try await useCase.execute(from: "merged.mp4")
        XCTAssertEqual(result, "thumb_merged.mp4.jpg")
        XCTAssertEqual(extractor.lastExtractTime, 0)
    }

    func testExtractionFailureThrows() async {
        let extractor = MockFrameExtractor(shouldSucceed: false)
        let useCase = GenerateThumbnailUseCase(frameExtractor: extractor)
        do {
            _ = try await useCase.execute(from: "merged.mp4")
            XCTFail("Expected error")
        } catch is ThumbnailError {
            XCTAssertEqual(extractor.lastExtractTime, 0)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }
}

final class MockFrameExtractor: FrameExtractorProtocol {
    private let shouldSucceed: Bool
    private(set) var lastExtractTime: Double?

    init(shouldSucceed: Bool) {
        self.shouldSucceed = shouldSucceed
    }

    func extractFrame(from videoURI: String, at time: Double) async throws -> String {
        lastExtractTime = time
        if shouldSucceed {
            return "thumb_\(videoURI).jpg"
        }
        throw ThumbnailError.extractionFailed("Mock failure")
    }
}
