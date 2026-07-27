package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.error.ValidateTrimDurationError

class ValidateTrimDurationUseCase {
    private val freeTierAllowed = setOf(1.0, 2.0, 3.0)
    private val paidTierRange = 1.0..5.0

    fun execute(trimDuration: Double, isPaidUser: Boolean) {
        if (trimDuration !in paidTierRange) {
            if (trimDuration < 1.0) {
                throw ValidateTrimDurationError.BelowMinimumDuration(trimDuration)
            }
            throw ValidateTrimDurationError.ExceedsMaxDuration(trimDuration)
        }
        if (!isPaidUser && trimDuration !in freeTierAllowed) {
            throw ValidateTrimDurationError.FreeTierRejectsCustomDuration(trimDuration)
        }
    }

    fun isAllowed(trimDuration: Double, isPaidUser: Boolean): Boolean {
        return try {
            execute(trimDuration, isPaidUser)
            true
        } catch (_: ValidateTrimDurationError) {
            false
        }
    }
}