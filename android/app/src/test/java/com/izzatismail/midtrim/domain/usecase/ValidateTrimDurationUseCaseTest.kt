package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.error.ValidateTrimDurationError
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateTrimDurationUseCaseTest {
    private val useCase = ValidateTrimDurationUseCase()

    @Test
    fun `free tier accepts 1s`() {
        useCase.execute(trimDuration = 1.0, isPaidUser = false)
    }

    @Test
    fun `free tier accepts 2s`() {
        useCase.execute(trimDuration = 2.0, isPaidUser = false)
    }

    @Test
    fun `free tier accepts 3s`() {
        useCase.execute(trimDuration = 3.0, isPaidUser = false)
    }

    @Test
    fun `free tier rejects custom`() {
        val error = assertThrows(ValidateTrimDurationError.FreeTierRejectsCustomDuration::class.java) {
            useCase.execute(trimDuration = 4.0, isPaidUser = false)
        }
        assertTrue(error is ValidateTrimDurationError)
    }

    @Test
    fun `paid tier accepts 1s`() {
        useCase.execute(trimDuration = 1.0, isPaidUser = true)
    }

    @Test
    fun `paid tier accepts 5s`() {
        useCase.execute(trimDuration = 5.0, isPaidUser = true)
    }

    @Test
    fun `paid tier accepts custom value`() {
        useCase.execute(trimDuration = 3.7, isPaidUser = true)
    }

    @Test
    fun `below minimum throws`() {
        val error = assertThrows(ValidateTrimDurationError.BelowMinimumDuration::class.java) {
            useCase.execute(trimDuration = 0.5, isPaidUser = true)
        }
        assertTrue(error is ValidateTrimDurationError)
    }

    @Test
    fun `exceeds max throws`() {
        val error = assertThrows(ValidateTrimDurationError.ExceedsMaxDuration::class.java) {
            useCase.execute(trimDuration = 5.5, isPaidUser = true)
        }
        assertTrue(error is ValidateTrimDurationError)
    }

    @Test
    fun `isAllowed free tier`() {
        assertTrue(useCase.isAllowed(trimDuration = 1.0, isPaidUser = false))
        assertTrue(useCase.isAllowed(trimDuration = 3.0, isPaidUser = false))
        assertFalse(useCase.isAllowed(trimDuration = 4.0, isPaidUser = false))
        assertFalse(useCase.isAllowed(trimDuration = 0.5, isPaidUser = false))
    }

    @Test
    fun `isAllowed paid tier`() {
        assertTrue(useCase.isAllowed(trimDuration = 1.0, isPaidUser = true))
        assertTrue(useCase.isAllowed(trimDuration = 3.5, isPaidUser = true))
        assertTrue(useCase.isAllowed(trimDuration = 5.0, isPaidUser = true))
        assertFalse(useCase.isAllowed(trimDuration = 0.5, isPaidUser = true))
        assertFalse(useCase.isAllowed(trimDuration = 5.5, isPaidUser = true))
    }
}