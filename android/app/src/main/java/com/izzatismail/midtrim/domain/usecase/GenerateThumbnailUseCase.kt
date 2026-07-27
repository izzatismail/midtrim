package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.repository.FrameExtractor

class GenerateThumbnailUseCase(
    private val frameExtractor: FrameExtractor
) {
    suspend fun execute(videoUri: String): String {
        return frameExtractor.extractFrame(videoUri, atTime = 0.0)
    }
}