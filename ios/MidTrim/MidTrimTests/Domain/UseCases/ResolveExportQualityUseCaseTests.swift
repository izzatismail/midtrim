import XCTest
import CoreGraphics
@testable import MidTrim

final class ResolveExportQualityUseCaseTests: XCTestCase {
    let useCase = ResolveExportQualityUseCase()

    func testFreeTierAlways720p() {
        let result = useCase.execute(isPaidUser: false, sourceResolution: CGSize(width: 1920, height: 1080))
        XCTAssertEqual(result, .free720p)
    }

    func testFreeTierWithLowResSource() {
        let result = useCase.execute(isPaidUser: false, sourceResolution: CGSize(width: 640, height: 480))
        XCTAssertEqual(result, .free720p)
    }

    func testFreeTierWith4KSource() {
        let result = useCase.execute(isPaidUser: false, sourceResolution: CGSize(width: 3840, height: 2160))
        XCTAssertEqual(result, .free720p)
    }

    func testPaidTierReturnsSourceResolution() {
        let source = CGSize(width: 1920, height: 1080)
        let result = useCase.execute(isPaidUser: true, sourceResolution: source)
        XCTAssertEqual(result, .paidOriginal(resolution: source))
    }

    func testPaidTierNeverUpscales() {
        let source = CGSize(width: 640, height: 480)
        let result = useCase.execute(isPaidUser: true, sourceResolution: source)
        XCTAssertEqual(result, .paidOriginal(resolution: source))
    }

    func testPaidTierWith4KSource() {
        let source = CGSize(width: 3840, height: 2160)
        let result = useCase.execute(isPaidUser: true, sourceResolution: source)
        XCTAssertEqual(result, .paidOriginal(resolution: source))
    }
}
