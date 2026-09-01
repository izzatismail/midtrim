import XCTest
import CoreGraphics
@testable import MidTrim

final class ResolveExportQualityUseCaseTests: XCTestCase {
    let useCase = ResolveExportQualityUseCase()

    func testFreeTierAlways720p() {
        let result = useCase.execute(isPaidUser: false, preset: nil, sourceResolution: CGSize(width: 1920, height: 1080))
        XCTAssertEqual(result, .free720p)
    }

    func testFreeTierIgnoresPreset() {
        let result = useCase.execute(isPaidUser: false, preset: .best, sourceResolution: CGSize(width: 1920, height: 1080))
        XCTAssertEqual(result, .free720p)
    }

    func testFreeTierWithLowResSource() {
        let result = useCase.execute(isPaidUser: false, preset: nil, sourceResolution: CGSize(width: 640, height: 480))
        XCTAssertEqual(result, .free720p)
    }

    func testFreeTierWith4KSource() {
        let result = useCase.execute(isPaidUser: false, preset: nil, sourceResolution: CGSize(width: 3840, height: 2160))
        XCTAssertEqual(result, .free720p)
    }

    func testPaidTierDefaultsToBestWhenPresetIsNil() {
        let source = CGSize(width: 1920, height: 1080)
        let result = useCase.execute(isPaidUser: true, preset: nil, sourceResolution: source)
        XCTAssertEqual(result, .paid(preset: .best, sourceResolution: source))
    }

    func testPaidTierBESTReturnsSourceResolution() {
        let source = CGSize(width: 1920, height: 1080)
        let result = useCase.execute(isPaidUser: true, preset: .best, sourceResolution: source)
        XCTAssertEqual(result, .paid(preset: .best, sourceResolution: source))
    }

    func testPaidTierSMALLWith4KSource() {
        let source = CGSize(width: 3840, height: 2160)
        let result = useCase.execute(isPaidUser: true, preset: .small, sourceResolution: source)
        XCTAssertEqual(result, .paid(preset: .small, sourceResolution: source))
    }

    func testPaidTierBALANCEDWith4KSource() {
        let source = CGSize(width: 3840, height: 2160)
        let result = useCase.execute(isPaidUser: true, preset: .balanced, sourceResolution: source)
        XCTAssertEqual(result, .paid(preset: .balanced, sourceResolution: source))
    }

    func testPaidTierBALANCEDWith480pSource() {
        let source = CGSize(width: 640, height: 480)
        let result = useCase.execute(isPaidUser: true, preset: .balanced, sourceResolution: source)
        XCTAssertEqual(result, .paid(preset: .balanced, sourceResolution: source))
    }
}
