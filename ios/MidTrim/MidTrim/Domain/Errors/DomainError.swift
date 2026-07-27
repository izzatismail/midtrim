import Foundation

enum TrimWindowError: LocalizedError, Equatable {
    case videoShorterThanTrimDuration(videoDuration: Double, trimDuration: Double)

    var errorDescription: String? {
        switch self {
        case let .videoShorterThanTrimDuration(videoDuration, trimDuration):
            "Video duration (\(videoDuration)s) is shorter than the selected trim duration (\(trimDuration)s)."
        }
    }
}

enum ValidateTrimDurationError: LocalizedError, Equatable {
    case freeTierRejectsCustomDuration(Double)
    case exceedsMaxDuration(Double)
    case belowMinimumDuration(Double)

    var errorDescription: String? {
        switch self {
        case let .freeTierRejectsCustomDuration(duration):
            "Free tier only supports 1s, 2s, or 3s trims. \(duration)s requires the paid tier."
        case let .exceedsMaxDuration(duration):
            "\(duration)s exceeds the maximum trim duration of 5.0s."
        case let .belowMinimumDuration(duration):
            "\(duration)s is below the minimum trim duration of 1.0s."
        }
    }
}

enum ResolveExportQualityError: LocalizedError, Equatable {
    var errorDescription: String? {
        "Could not determine export resolution from the source video."
    }
}

enum ReorderVideosError: LocalizedError, Equatable {
    case invalidIndices
    case emptyList

    var errorDescription: String? {
        switch self {
        case .invalidIndices:
            "The provided indices do not match the video count."
        case .emptyList:
            "Cannot reorder an empty list."
        }
    }
}

enum ProjectError: LocalizedError, Equatable {
    case notFound(UUID)
    case duplicateName(String)
    case emptyName
    case saveFailed(String)
    case deleteFailed(String)

    var errorDescription: String? {
        switch self {
        case let .notFound(id):
            "Project with id \(id) was not found."
        case let .duplicateName(name):
            "A project named \"\(name)\" already exists."
        case .emptyName:
            "Project name cannot be empty."
        case let .saveFailed(reason):
            "Failed to save project: \(reason)"
        case let .deleteFailed(reason):
            "Failed to delete project: \(reason)"
        }
    }
}

enum ImportVideoError: LocalizedError, Equatable {
    case unsupportedFormat(String)
    case exceedsTierCap(max: Int, attempted: Int)
    case emptySelection
    case metadataFetchFailed(String)

    var errorDescription: String? {
        switch self {
        case let .unsupportedFormat(uri):
            "Video at \(uri) uses an unsupported format."
        case let .exceedsTierCap(max, attempted):
            "You can select at most \(max) videos (attempted \(attempted))."
        case .emptySelection:
            "Please select at least one video."
        case let .metadataFetchFailed(reason):
            "Failed to read video metadata: \(reason)"
        }
    }
}

enum TrimError: LocalizedError, Equatable {
    case fileNotFound(String)
    case exportFailed(String)
    case durationRejected(Double)
    case mergeFailed(String)

    var errorDescription: String? {
        switch self {
        case let .fileNotFound(uri):
            "Video file not found at \(uri)."
        case let .exportFailed(reason):
            "Video export failed: \(reason)"
        case let .durationRejected(duration):
            "Trim duration \(duration)s is not allowed for your tier."
        case let .mergeFailed(reason):
            "Merge failed: \(reason)"
        }
    }
}

enum ThumbnailError: LocalizedError, Equatable {
    case extractionFailed(String)

    var errorDescription: String? {
        switch self {
        case let .extractionFailed(reason):
            "Thumbnail extraction failed: \(reason)"
        }
    }
}
