import XCTest
@testable import MidTrim

final class TrimVideoUseCaseTests: XCTestCase {
    func testTrimSuccess() async throws {
        let trimmer = MockVideoTrimmer()
        let useCase = TrimVideoUseCase(
            trimmer: trimmer,
            trimValidator: ValidateTrimDurationUseCase(),
            windowCalculator: CalculateTrimWindowUseCase()
        )
        let result = try await useCase.execute(sourceURI: "v.mp4", duration: 10, trimDuration: 3, isPaidUser: false)
        XCTAssertEqual(result, "trimmed_v.mp4")
    }

    func testRejectedDurationThrows() async {
        let trimmer = MockVideoTrimmer()
        let useCase = TrimVideoUseCase(
            trimmer: trimmer,
            trimValidator: ValidateTrimDurationUseCase(),
            windowCalculator: CalculateTrimWindowUseCase()
        )
        do {
            _ = try await useCase.execute(sourceURI: "v.mp4", duration: 10, trimDuration: 4, isPaidUser: false)
            XCTFail("Expected error")
        } catch is ValidateTrimDurationError {
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testVideoShorterThanTrimThrows() async {
        let trimmer = MockVideoTrimmer()
        let useCase = TrimVideoUseCase(
            trimmer: trimmer,
            trimValidator: ValidateTrimDurationUseCase(),
            windowCalculator: CalculateTrimWindowUseCase()
        )
        do {
            _ = try await useCase.execute(sourceURI: "v.mp4", duration: 2, trimDuration: 5, isPaidUser: true)
            XCTFail("Expected error")
        } catch is TrimWindowError {
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testEmptySourceURIThrows() async {
        let trimmer = MockVideoTrimmer()
        let useCase = TrimVideoUseCase(
            trimmer: trimmer,
            trimValidator: ValidateTrimDurationUseCase(),
            windowCalculator: CalculateTrimWindowUseCase()
        )
        do {
            _ = try await useCase.execute(sourceURI: "", duration: 10, trimDuration: 3, isPaidUser: false)
            XCTFail("Expected error")
        } catch {
            XCTAssertTrue(error is TrimError || error is NSError)
        }
    }
}

final class MockVideoTrimmer: VideoTrimmerProtocol {
    func trim(sourceURI: String, startTime: Double, endTime: Double) async throws -> String {
        "trimmed_\(sourceURI)"
    }

    func merge(segmentURIs: [String], outputQuality: ExportQuality) async throws -> String {
        "merged.mp4"
    }
}
