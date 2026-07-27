package com.izzatismail.midtrim.domain.usecase

class CalculateMergedDurationUseCase {
    fun execute(trimDuration: Double, videoCount: Int): Double {
        if (trimDuration < 0 || videoCount < 0) return 0.0
        return trimDuration * videoCount
    }
}