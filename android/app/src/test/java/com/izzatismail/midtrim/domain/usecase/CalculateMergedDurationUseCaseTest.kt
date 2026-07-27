package com.izzatismail.midtrim.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateMergedDurationUseCaseTest {
    private val useCase = CalculateMergedDurationUseCase()

    @Test
    fun `single video`() {
        val result = useCase.execute(trimDuration = 3.0, videoCount = 1)
        assertEquals(3.0, result, 0.001)
    }

    @Test
    fun `ten videos`() {
        val result = useCase.execute(trimDuration = 2.0, videoCount = 10)
        assertEquals(20.0, result, 0.001)
    }

    @Test
    fun `twenty videos`() {
        val result = useCase.execute(trimDuration = 1.0, videoCount = 20)
        assertEquals(20.0, result, 0.001)
    }

    @Test
    fun `custom duration`() {
        val result = useCase.execute(trimDuration = 4.5, videoCount = 3)
        assertEquals(13.5, result, 0.001)
    }

    @Test
    fun `zero videos`() {
        val result = useCase.execute(trimDuration = 3.0, videoCount = 0)
        assertEquals(0.0, result, 0.001)
    }
}