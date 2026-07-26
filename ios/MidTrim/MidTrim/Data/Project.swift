import Foundation
import SwiftData

@Model
final class Project {
    var name: String
    var trimDuration: Double
    var wasCustomDuration: Bool
    var exportQualityTier: String
    var createdAt: Date
    var outputVideoPath: String?
    var thumbnailPath: String?

    @Relationship(deleteRule: .cascade, inverse: \SourceVideoItem.project)
    var sourceVideos: [SourceVideoItem]?

    init(
        name: String,
        trimDuration: Double,
        wasCustomDuration: Bool,
        exportQualityTier: String,
        createdAt: Date = Date(),
        outputVideoPath: String? = nil,
        thumbnailPath: String? = nil
    ) {
        self.name = name
        self.trimDuration = trimDuration
        self.wasCustomDuration = wasCustomDuration
        self.exportQualityTier = exportQualityTier
        self.createdAt = createdAt
        self.outputVideoPath = outputVideoPath
        self.thumbnailPath = thumbnailPath
    }
}
