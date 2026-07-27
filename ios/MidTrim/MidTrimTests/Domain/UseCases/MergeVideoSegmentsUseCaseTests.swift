import XCTest
import CoreGraphics
@testable import MidTrim

final class MergeVideoSegmentsUseCaseTests: XCTestCase {
    func testMergeSuccess() async throws {
        let trimmer = MockVideoTrimmer()
        let useCase = MergeVideoSegmentsUseCase(
            trimmer: trimmer,
            qualityResolver: ResolveExportQualityUseCase()
        )
        let result = try await useCase.execute(
            segmentURIs: ["s1.mp4", "s2.mp4"],
            isPaidUser: false,
            sourceResolution: CGSize(width: 1920, height: 1080)
        )
        XCTAssertEqual(result, "merged.mp4")
    }

    func testEmptySegmentsThrows() async {
        let trimmer = MockVideoTrimmer()
        let useCase = MergeVideoSegmentsUseCase(
            trimmer: trimmer,
            qualityResolver: ResolveExportQualityUseCase()
        )
        do {
            _ = try await useCase.execute(
                segmentURIs: [],
                isPaidUser: false,
                sourceResolution: CGSize(width: 1920, height: 1080)
            )
            XCTFail("Expected error")
        } catch let error as TrimError {
            guard case .mergeFailed = error else {
                return XCTFail("Wrong error case")
            }
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testSingleSegment() async throws {
        let trimmer = MockVideoTrimmer()
        let useCase = MergeVideoSegmentsUseCase(
            trimmer: trimmer,
            qualityResolver: ResolveExportQualityUseCase()
        )
        let result = try await useCase.execute(
            segmentURIs: ["s1.mp4"],
            isPaidUser: true,
            sourceResolution: CGSize(width: 3840, height: 2160)
        )
        XCTAssertEqual(result, "merged.mp4")
    }
}
