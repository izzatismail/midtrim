import XCTest
@testable import MidTrim

final class ModelMappersTests: XCTestCase {
    func testProjectModelToDomainRoundTrip() {
        let modelId = UUID(uuidString: "E621E1F8-C36C-495A-93FC-0C247A3E6E5F") ?? UUID()
        let model = Project(
            id: modelId,
            name: "Test",
            trimDuration: 3.0,
            wasCustomDuration: false,
            outputVideoURI: "out.mp4",
            thumbnailURI: "thumb.jpg",
            exportQualityTier: "free_720p",
            mergedDuration: 3.0,
            videoCount: 1,
            createdAt: Date(timeIntervalSince1970: 1000),
            updatedAt: Date(timeIntervalSince1970: 2000)
        )
        let domain = model.toDomain()
        let backToModel = domain.toModel()

        XCTAssertEqual(model.id, backToModel.id)
        XCTAssertEqual(model.name, backToModel.name)
        XCTAssertEqual(model.trimDuration, backToModel.trimDuration)
        XCTAssertEqual(model.wasCustomDuration, backToModel.wasCustomDuration)
        XCTAssertEqual(model.outputVideoURI, backToModel.outputVideoURI)
        XCTAssertEqual(model.thumbnailURI, backToModel.thumbnailURI)
        XCTAssertEqual(model.exportQualityTier, backToModel.exportQualityTier)
        XCTAssertEqual(model.mergedDuration, backToModel.mergedDuration)
        XCTAssertEqual(model.videoCount, backToModel.videoCount)
        XCTAssertEqual(model.createdAt, backToModel.createdAt)
        XCTAssertEqual(model.updatedAt, backToModel.updatedAt)
    }

    func testSourceVideoItemModelToDomainRoundTrip() {
        let projectId = UUID(uuidString: "E621E1F8-C36C-495A-93FC-0C247A3E6E5F") ?? UUID()
        let project = Project(
            id: projectId,
            name: "P",
            trimDuration: 3.0,
            wasCustomDuration: false,
            outputVideoURI: "o.mp4",
            thumbnailURI: "t.jpg",
            exportQualityTier: "free_720p",
            mergedDuration: 3.0,
            videoCount: 1
        )
        let videoId = UUID(uuidString: "A621E1F8-C36C-495A-93FC-0C247A3E6E5F") ?? UUID()
        let model = SourceVideoItem(
            id: videoId,
            sourceVideoURI: "source.mp4",
            sourceVideoDuration: 10.0,
            sourceFileSize: 1024,
            orderIndex: 0,
            trimStartTime: 3.5,
            trimEndTime: 6.5
        )
        model.project = project
        let domain = model.toDomain()
        let backToModel = domain.toModel()

        XCTAssertEqual(model.id, backToModel.id)
        XCTAssertEqual(model.sourceVideoURI, backToModel.sourceVideoURI)
        XCTAssertEqual(model.sourceVideoDuration, backToModel.sourceVideoDuration)
        XCTAssertEqual(model.sourceFileSize, backToModel.sourceFileSize)
        XCTAssertEqual(model.orderIndex, backToModel.orderIndex)
        XCTAssertEqual(model.trimStartTime, backToModel.trimStartTime, accuracy: 0.001)
        XCTAssertEqual(model.trimEndTime, backToModel.trimEndTime, accuracy: 0.001)
    }

    func testSourceVideoItemNullFileSizeRoundTrip() {
        let model = SourceVideoItem(
            id: UUID(),
            sourceVideoURI: "source.mp4",
            sourceVideoDuration: 5.0,
            sourceFileSize: nil,
            orderIndex: 1,
            trimStartTime: 1.0,
            trimEndTime: 4.0
        )
        let domain = model.toDomain()
        let backToModel = domain.toModel()
        XCTAssertNil(backToModel.sourceFileSize)
    }

    func testDomainToModelPreservesExportQualityTier() {
        let domain = ProjectInfo(
            id: UUID(),
            name: "Paid",
            trimDuration: 4.5,
            wasCustomDuration: true,
            outputVideoURI: "out.mp4",
            thumbnailURI: "thumb.jpg",
            exportQualityTier: "paid_original",
            mergedDuration: 9.0,
            videoCount: 2,
            createdAt: Date(),
            updatedAt: Date()
        )
        let model = domain.toModel()
        XCTAssertEqual(model.exportQualityTier, "paid_original")
        XCTAssertEqual(model.wasCustomDuration, true)
    }
}
