package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ExportQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveExportQualityUseCaseTest {
    private val useCase = ResolveExportQualityUseCase()

    @Test
    fun `free tier always 720p`() {
        val result = useCase.execute(isPaidUser = false, sourceWidth = 1920, sourceHeight = 1080)
        assertTrue(result is ExportQuality.Free720p)
    }

    @Test
    fun `free tier with low res source`() {
        val result = useCase.execute(isPaidUser = false, sourceWidth = 640, sourceHeight = 480)
        assertTrue(result is ExportQuality.Free720p)
    }

    @Test
    fun `free tier with 4K source`() {
        val result = useCase.execute(isPaidUser = false, sourceWidth = 3840, sourceHeight = 2160)
        assertTrue(result is ExportQuality.Free720p)
    }

    @Test
    fun `paid tier returns source resolution`() {
        val result = useCase.execute(isPaidUser = true, sourceWidth = 1920, sourceHeight = 1080)
        val paid = result as ExportQuality.PaidOriginal
        assertEquals(1920, paid.resolutionWidth)
        assertEquals(1080, paid.resolutionHeight)
    }

    @Test
    fun `paid tier never upscales`() {
        val result = useCase.execute(isPaidUser = true, sourceWidth = 640, sourceHeight = 480)
        val paid = result as ExportQuality.PaidOriginal
        assertEquals(640, paid.resolutionWidth)
        assertEquals(480, paid.resolutionHeight)
    }

    @Test
    fun `paid tier with 4K source`() {
        val result = useCase.execute(isPaidUser = true, sourceWidth = 3840, sourceHeight = 2160)
        val paid = result as ExportQuality.PaidOriginal
        assertEquals(3840, paid.resolutionWidth)
        assertEquals(2160, paid.resolutionHeight)
    }
}