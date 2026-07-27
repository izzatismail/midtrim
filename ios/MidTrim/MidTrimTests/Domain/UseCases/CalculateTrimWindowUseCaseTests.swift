import XCTest
@testable import MidTrim

final class CalculateTrimWindowUseCaseTests: XCTestCase {
    let useCase = CalculateTrimWindowUseCase()

    func testTypicalTrim() throws {
        let window = try useCase.execute(videoDuration: 10, trimDuration: 4)
        XCTAssertEqual(window.startTime, 3, accuracy: 0.001)
        XCTAssertEqual(window.endTime, 7, accuracy: 0.001)
        XCTAssertEqual(window.duration, 4, accuracy: 0.001)
    }

    func testVideoEqualsTrimDuration() throws {
        let window = try useCase.execute(videoDuration: 3, trimDuration: 3)
        XCTAssertEqual(window.startTime, 0, accuracy: 0.001)
        XCTAssertEqual(window.endTime, 3, accuracy: 0.001)
    }

    func testTrimDurationOneSecond() throws {
        let window = try useCase.execute(videoDuration: 60, trimDuration: 1)
        XCTAssertEqual(window.startTime, 29.5, accuracy: 0.001)
        XCTAssertEqual(window.endTime, 30.5, accuracy: 0.001)
    }

    func testVeryShortVideo() throws {
        let window = try useCase.execute(videoDuration: 1.5, trimDuration: 1)
        XCTAssertEqual(window.startTime, 0.25, accuracy: 0.001)
        XCTAssertEqual(window.endTime, 1.25, accuracy: 0.001)
    }

    func testVideoShorterThanTrimThrows() {
        XCTAssertThrowsError(try useCase.execute(videoDuration: 2, trimDuration: 5)) { error in
            guard case let .videoShorterThanTrimDuration(videoDuration, trimDuration) = error as? TrimWindowError else {
                return XCTFail("Expected videoShorterThanTrimDuration")
            }
            XCTAssertEqual(videoDuration, 2)
            XCTAssertEqual(trimDuration, 5)
        }
    }

    func testExactEdgeCase() throws {
        let window = try useCase.execute(videoDuration: 5, trimDuration: 5)
        XCTAssertEqual(window.startTime, 0)
        XCTAssertEqual(window.endTime, 5)
    }
}
