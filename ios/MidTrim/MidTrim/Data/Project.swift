import Foundation
import SwiftData

@Model
final class Project {
    @Attribute(.unique) var id: UUID
    var name: String
    var trimDuration: Double
    var wasCustomDuration: Bool
    var outputVideoURI: String
    var thumbnailURI: String
    var exportQualityTier: String
    var mergedDuration: Double
    var videoCount: Int
    var createdAt: Date
    var updatedAt: Date

    @Relationship(deleteRule: .cascade, inverse: \SourceVideoItem.project)
    var sourceVideos: [SourceVideoItem] = []

    init(
        id: UUID = UUID(),
        name: String,
        trimDuration: Double,
        wasCustomDuration: Bool,
        outputVideoURI: String,
        thumbnailURI: String,
        exportQualityTier: String,
        mergedDuration: Double,
        videoCount: Int,
        createdAt: Date = .now,
        updatedAt: Date = .now
    ) {
        self.id = id
        self.name = name
        self.trimDuration = trimDuration
        self.wasCustomDuration = wasCustomDuration
        self.outputVideoURI = outputVideoURI
        self.thumbnailURI = thumbnailURI
        self.exportQualityTier = exportQualityTier
        self.mergedDuration = mergedDuration
        self.videoCount = videoCount
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}
