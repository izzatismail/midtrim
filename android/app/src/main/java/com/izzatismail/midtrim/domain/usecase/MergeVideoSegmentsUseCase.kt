package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ExportQuality
import com.izzatismail.midtrim.domain.error.TrimError
import com.izzatismail.midtrim.domain.repository.VideoTrimmer

class MergeVideoSegmentsUseCase(
    private val trimmer: VideoTrimmer,
    private val qualityResolver: ResolveExportQualityUseCase
) {
    suspend fun execute(
        segmentUris: List<String>,
        isPaidUser: Boolean,
        sourceWidth: Int,
        sourceHeight: Int
    ): String {
        if (segmentUris.isEmpty()) throw TrimError.MergeFailed("No segments to merge.")

        val quality: ExportQuality = qualityResolver.execute(isPaidUser, sourceWidth, sourceHeight)
        return trimmer.merge(segmentUris, quality)
    }
}