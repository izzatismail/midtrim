package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.error.TrimError
import com.izzatismail.midtrim.domain.repository.VideoTrimmer

class TrimVideoUseCase(
    private val trimmer: VideoTrimmer,
    private val trimValidator: ValidateTrimDurationUseCase,
    private val windowCalculator: CalculateTrimWindowUseCase
) {
    suspend fun execute(
        sourceUri: String,
        videoDuration: Double,
        trimDuration: Double,
        isPaidUser: Boolean
    ): String {
        require(sourceUri.isNotEmpty()) { "sourceUri must not be empty" }
        trimValidator.execute(trimDuration, isPaidUser)
        val window = windowCalculator.execute(videoDuration, trimDuration)
        return trimmer.trim(sourceUri, window.startTime, window.endTime)
    }
}