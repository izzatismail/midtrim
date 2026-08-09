import Foundation

extension Project {
    func toDomain() -> ProjectInfo {
        ProjectInfo(
            id: id,
            name: name,
            trimDuration: trimDuration,
            wasCustomDuration: wasCustomDuration,
            outputVideoURI: outputVideoURI,
            thumbnailURI: thumbnailURI,
            exportQualityTier: exportQualityTier,
            mergedDuration: mergedDuration,
            videoCount: videoCount,
            createdAt: createdAt,
            updatedAt: updatedAt
        )
    }
}

extension ProjectInfo {
    func toModel() -> Project {
        Project(
            id: id,
            name: name,
            trimDuration: trimDuration,
            wasCustomDuration: wasCustomDuration,
            outputVideoURI: outputVideoURI,
            thumbnailURI: thumbnailURI,
            exportQualityTier: exportQualityTier,
            mergedDuration: mergedDuration,
            videoCount: videoCount,
            createdAt: createdAt,
            updatedAt: updatedAt
        )
    }
}

extension SourceVideoItem {
    func toDomain() -> SourceVideoInfo {
        SourceVideoInfo(
            id: id,
            projectId: project?.id ?? UUID(),
            sourceVideoURI: sourceVideoURI,
            sourceVideoDuration: sourceVideoDuration,
            sourceFileSize: sourceFileSize,
            orderIndex: orderIndex,
            trimStartTime: trimStartTime,
            trimEndTime: trimEndTime
        )
    }
}

extension SourceVideoInfo {
    func toModel() -> SourceVideoItem {
        SourceVideoItem(
            id: id,
            sourceVideoURI: sourceVideoURI,
            sourceVideoDuration: sourceVideoDuration,
            sourceFileSize: sourceFileSize,
            orderIndex: orderIndex,
            trimStartTime: trimStartTime,
            trimEndTime: trimEndTime
        )
    }
}
