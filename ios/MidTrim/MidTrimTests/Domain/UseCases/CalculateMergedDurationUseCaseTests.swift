import XCTest
@testable import MidTrim

final class CalculateMergedDurationUseCaseTests: XCTestCase {
    let useCase = CalculateMergedDurationUseCase()

    func testSingleVideo() {
        let result = useCase.execute(trimDuration: 3, videoCount: 1)
        XCTAssertEqual(result, 3)
    }

    func testTenVideos() {
        let result = useCase.execute(trimDuration: 2, videoCount: 10)
        XCTAssertEqual(result, 20)
    }

    func testTwentyVideos() {
        let result = useCase.execute(trimDuration: 1, videoCount: 20)
        XCTAssertEqual(result, 20)
    }

    func testCustomDuration() {
        let result = useCase.execute(trimDuration: 4.5, videoCount: 3)
        XCTAssertEqual(result, 13.5)
    }

    func testZeroVideos() {
        let result = useCase.execute(trimDuration: 3, videoCount: 0)
        XCTAssertEqual(result, 0)
    }
}
