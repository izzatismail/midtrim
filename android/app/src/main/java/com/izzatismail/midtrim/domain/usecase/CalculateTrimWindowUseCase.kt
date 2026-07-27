package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.TrimWindow
import com.izzatismail.midtrim.domain.error.TrimWindowError

class CalculateTrimWindowUseCase {
    fun execute(videoDuration: Double, trimDuration: Double): TrimWindow {
        if (videoDuration < trimDuration) {
            throw TrimWindowError.VideoShorterThanTrimDuration(videoDuration, trimDuration)
        }

        val center = videoDuration / 2
        val halfTrim = trimDuration / 2
        val startTime = maxOf(0.0, center - halfTrim)
        val endTime = minOf(videoDuration, center + halfTrim)

        return TrimWindow(startTime = startTime, endTime = endTime)
    }
}