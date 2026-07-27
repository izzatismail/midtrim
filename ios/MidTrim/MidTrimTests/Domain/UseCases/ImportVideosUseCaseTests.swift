import XCTest
import CoreGraphics
@testable import MidTrim

final class ImportVideosUseCaseTests: XCTestCase {
    func testImportSuccess() async throws {
        let service = MockVideoMetadataService()
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = ImportVideosUseCase(metadataService: service, entitlementCache: FetchEntitlementStatusUseCase(cache: cache))
        let uris = ["video1.mp4", "video2.mp4"]
        let result = try await useCase.execute(videoURIs: uris)
        XCTAssertEqual(result.count, 2)
        XCTAssertEqual(result[0].uri, "video1.mp4")
    }

    func testEmptySelectionThrows() async {
        let service = MockVideoMetadataService()
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = ImportVideosUseCase(metadataService: service, entitlementCache: FetchEntitlementStatusUseCase(cache: cache))
        do {
            _ = try await useCase.execute(videoURIs: [])
            XCTFail("Expected emptySelection error")
        } catch let error as ImportVideoError {
            XCTAssertEqual(error, .emptySelection)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testFreeTierExceedsCapThrows() async {
        let service = MockVideoMetadataService()
        let cache = MockEntitlementCache(isPurchased: false)
        let useCase = ImportVideosUseCase(metadataService: service, entitlementCache: FetchEntitlementStatusUseCase(cache: cache))
        let uris = Array(repeating: "v.mp4", count: 11)
        do {
            _ = try await useCase.execute(videoURIs: uris)
            XCTFail("Expected exceedsTierCap error")
        } catch let error as ImportVideoError {
            guard case let .exceedsTierCap(max, attempted) = error else {
                return XCTFail("Wrong error case")
            }
            XCTAssertEqual(max, 10)
            XCTAssertEqual(attempted, 11)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }

    func testPaidTierAllows20() async throws {
        let service = MockVideoMetadataService()
        let cache = MockEntitlementCache(isPurchased: true)
        let useCase = ImportVideosUseCase(metadataService: service, entitlementCache: FetchEntitlementStatusUseCase(cache: cache))
        let uris = Array(repeating: "v.mp4", count: 20)
        let result = try await useCase.execute(videoURIs: uris)
        XCTAssertEqual(result.count, 20)
    }

    func testPaidTierExceedsCapThrows() async {
        let service = MockVideoMetadataService()
        let cache = MockEntitlementCache(isPurchased: true)
        let useCase = ImportVideosUseCase(metadataService: service, entitlementCache: FetchEntitlementStatusUseCase(cache: cache))
        let uris = Array(repeating: "v.mp4", count: 21)
        do {
            _ = try await useCase.execute(videoURIs: uris)
            XCTFail("Expected exceedsTierCap error")
        } catch let error as ImportVideoError {
            guard case let .exceedsTierCap(max, attempted) = error else {
                return XCTFail("Wrong error case")
            }
            XCTAssertEqual(max, 20)
            XCTAssertEqual(attempted, 21)
        } catch {
            XCTFail("Unexpected error: \(error)")
        }
    }
}

final class MockVideoMetadataService: VideoMetadataServiceProtocol {
    func fetchMetadata(for videoURI: String) async throws -> VideoMetadata {
        VideoMetadata(
            uri: videoURI, duration: 10.0,
            resolution: CGSize(width: 1920, height: 1080),
            fileSize: 1_000_000, format: "mp4"
        )
    }
}
