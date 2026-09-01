import XCTest
import CoreGraphics
@testable import MidTrim

final class ExportQualityMapperTests: XCTestCase {

    func testFree720pToTierString() {
        XCTAssertEqual(ExportQuality.free720p.toTierString(), "free_720p")
    }

    func testPaid720pSmallToTierString() {
        let quality = ExportQuality.paid(preset: .small, sourceResolution: CGSize(width: 1920, height: 1080))
        XCTAssertEqual(quality.toTierString(), "paid_720p")
    }

    func testPaid1080pBalancedToTierString() {
        let quality = ExportQuality.paid(preset: .balanced, sourceResolution: CGSize(width: 3840, height: 2160))
        XCTAssertEqual(quality.toTierString(), "paid_1080p")
    }

    func testPaidOriginalBestToTierString() {
        let quality = ExportQuality.paid(preset: .best, sourceResolution: CGSize(width: 1920, height: 1080))
        XCTAssertEqual(quality.toTierString(), "paid_original")
    }

    func testTierStringFree720pToExportQuality() {
        let quality = ExportQuality.from(tierString: "free_720p", sourceResolution: CGSize(width: 1920, height: 1080))
        XCTAssertEqual(quality, .free720p)
    }

    func testTierStringPaid720pToExportQuality() {
        let source = CGSize(width: 1920, height: 1080)
        let quality = ExportQuality.from(tierString: "paid_720p", sourceResolution: source)
        XCTAssertEqual(quality, .paid(preset: .small, sourceResolution: source))
    }

    func testTierStringPaid1080pToExportQuality() {
        let source = CGSize(width: 3840, height: 2160)
        let quality = ExportQuality.from(tierString: "paid_1080p", sourceResolution: source)
        XCTAssertEqual(quality, .paid(preset: .balanced, sourceResolution: source))
    }

    func testTierStringPaidOriginalToExportQuality() {
        let source = CGSize(width: 1920, height: 1080)
        let quality = ExportQuality.from(tierString: "paid_original", sourceResolution: source)
        XCTAssertEqual(quality, .paid(preset: .best, sourceResolution: source))
    }

    func testUnknownTierStringDefaultsToFree720p() {
        let quality = ExportQuality.from(tierString: "unknown_tier", sourceResolution: CGSize(width: 1920, height: 1080))
        XCTAssertEqual(quality, .free720p)
    }

    func testRoundTripAllTierStrings() {
        let testCases: [(String, CGSize)] = [
            ("free_720p", CGSize(width: 1920, height: 1080)),
            ("paid_720p", CGSize(width: 1920, height: 1080)),
            ("paid_1080p", CGSize(width: 3840, height: 2160)),
            ("paid_original", CGSize(width: 1920, height: 1080))
        ]
        for (tier, source) in testCases {
            let quality = ExportQuality.from(tierString: tier, sourceResolution: source)
            XCTAssertEqual(quality.toTierString(), tier)
        }
    }
}
