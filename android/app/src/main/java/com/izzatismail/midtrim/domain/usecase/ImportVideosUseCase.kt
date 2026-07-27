package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.VideoMetadata
import com.izzatismail.midtrim.domain.error.ImportVideoError
import com.izzatismail.midtrim.domain.repository.VideoMetadataService

class ImportVideosUseCase(
    private val metadataService: VideoMetadataService,
    private val fetchEntitlementStatus: FetchEntitlementStatusUseCase
) {
    private val maxVideos = 10
    private val maxVideosPaid = 20

    suspend fun execute(videoUris: List<String>): List<VideoMetadata> {
        if (videoUris.isEmpty()) throw ImportVideoError.EmptySelection

        val cap = if (fetchEntitlementStatus.isPaidUser) maxVideosPaid else maxVideos
        if (videoUris.size > cap) throw ImportVideoError.ExceedsTierCap(cap, videoUris.size)

        return videoUris.map { uri ->
            metadataService.fetchMetadata(uri)
        }
    }
}