package com.izzatismail.midtrim.domain.repository

import com.izzatismail.midtrim.domain.entity.ExportQuality
import com.izzatismail.midtrim.domain.entity.VideoMetadata

interface VideoTrimmer {
    suspend fun trim(sourceUri: String, startTime: Double, endTime: Double): String
    suspend fun merge(segmentUris: List<String>, outputQuality: ExportQuality): String
}

interface FrameExtractor {
    suspend fun extractFrame(videoUri: String, atTime: Double): String
}

interface VideoMetadataService {
    suspend fun fetchMetadata(videoUri: String): VideoMetadata
}