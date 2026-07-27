package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.error.TrimWindowError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateTrimWindowUseCaseTest {
    private val useCase = CalculateTrimWindowUseCase()

    @Test
    fun `typical trim`() {
        val window = useCase.execute(videoDuration = 10.0, trimDuration = 4.0)
        assertEquals(3.0, window.startTime, 0.001)
        assertEquals(7.0, window.endTime, 0.001)
        assertEquals(4.0, window.duration, 0.001)
    }

    @Test
    fun `video equals trim duration`() {
        val window = useCase.execute(videoDuration = 3.0, trimDuration = 3.0)
        assertEquals(0.0, window.startTime, 0.001)
        assertEquals(3.0, window.endTime, 0.001)
    }

    @Test
    fun `trim duration one second`() {
        val window = useCase.execute(videoDuration = 60.0, trimDuration = 1.0)
        assertEquals(29.5, window.startTime, 0.001)
        assertEquals(30.5, window.endTime, 0.001)
    }

    @Test
    fun `very short video`() {
        val window = useCase.execute(videoDuration = 1.5, trimDuration = 1.0)
        assertEquals(0.25, window.startTime, 0.001)
        assertEquals(1.25, window.endTime, 0.001)
    }

    @Test
    fun `video shorter than trim throws`() {
        val error = assertThrows(TrimWindowError.VideoShorterThanTrimDuration::class.java) {
            useCase.execute(videoDuration = 2.0, trimDuration = 5.0)
        }
        assertEquals(2.0, error.videoDuration, 0.001)
        assertEquals(5.0, error.trimDuration, 0.001)
    }

    @Test
    fun `exact edge case`() {
        val window = useCase.execute(videoDuration = 5.0, trimDuration = 5.0)
        assertEquals(0.0, window.startTime, 0.001)
        assertEquals(5.0, window.endTime, 0.001)
    }
}