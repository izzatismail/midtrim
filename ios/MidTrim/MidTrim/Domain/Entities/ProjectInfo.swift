import Foundation

struct ProjectInfo: Equatable {
    let id: UUID
    let name: String
    let trimDuration: Double
    let wasCustomDuration: Bool
    let outputVideoURI: String
    let thumbnailURI: String
    let exportQualityTier: String
    let mergedDuration: Double
    let videoCount: Int
    let createdAt: Date
    let updatedAt: Date
}

struct SourceVideoInfo: Equatable {
    let id: UUID
    let projectId: UUID
    let sourceVideoURI: String
    let sourceVideoDuration: Double
    let sourceFileSize: Int64?
    let orderIndex: Int
    let trimStartTime: Double
    let trimEndTime: Double
}
