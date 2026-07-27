package com.izzatismail.midtrim.domain.error

sealed class TrimWindowError : Exception() {
    class VideoShorterThanTrimDuration(
        val videoDuration: Double,
        val trimDuration: Double
    ) : TrimWindowError()
}

sealed class ValidateTrimDurationError : Exception() {
    class FreeTierRejectsCustomDuration(val duration: Double) : ValidateTrimDurationError()
    class ExceedsMaxDuration(val duration: Double) : ValidateTrimDurationError()
    class BelowMinimumDuration(val duration: Double) : ValidateTrimDurationError()
}

sealed class ResolveExportQualityError : Exception() {
}

sealed class ReorderVideosError : Exception() {
    data object InvalidIndices : ReorderVideosError()
    data object EmptyList : ReorderVideosError()
}

sealed class ProjectError : Exception() {
    data class NotFound(val id: String) : ProjectError()
    data class DuplicateName(val name: String) : ProjectError()
    data object EmptyName : ProjectError()
    data class SaveFailed(val reason: String) : ProjectError()
    data class DeleteFailed(val reason: String) : ProjectError()
}

sealed class ImportVideoError : Exception() {
    data class UnsupportedFormat(val uri: String) : ImportVideoError()
    data class ExceedsTierCap(val max: Int, val attempted: Int) : ImportVideoError()
    data object EmptySelection : ImportVideoError()
    data class MetadataFetchFailed(val reason: String) : ImportVideoError()
}

sealed class TrimError : Exception() {
    data class FileNotFound(val uri: String) : TrimError()
    data class ExportFailed(val reason: String) : TrimError()
    data class DurationRejected(val duration: Double) : TrimError()
    data class MergeFailed(val reason: String) : TrimError()
}

sealed class ThumbnailError : Exception() {
    data class ExtractionFailed(val reason: String) : ThumbnailError()
}