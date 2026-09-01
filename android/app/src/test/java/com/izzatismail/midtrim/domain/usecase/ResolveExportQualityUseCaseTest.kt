package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ExportQuality
import com.izzatismail.midtrim.domain.entity.ExportQualityPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveExportQualityUseCaseTest {
    private val useCase = ResolveExportQualityUseCase()

    @Test
    fun `free tier always 720p`() {
        val result = useCase.execute(isPaidUser = false, preset = null, sourceWidth = 1920, sourceHeight = 1080)
        assertTrue(result is ExportQuality.Free720p)
    }

    @Test
    fun `free tier ignores preset`() {
        val result = useCase.execute(isPaidUser = false, preset = ExportQualityPreset.BEST, sourceWidth = 1920, sourceHeight = 1080)
        assertTrue(result is ExportQuality.Free720p)
    }

    @Test
    fun `free tier with low res source`() {
        val result = useCase.execute(isPaidUser = false, preset = null, sourceWidth = 640, sourceHeight = 480)
        assertTrue(result is ExportQuality.Free720p)
    }

    @Test
    fun `free tier with 4K source`() {
        val result = useCase.execute(isPaidUser = false, preset = null, sourceWidth = 3840, sourceHeight = 2160)
        assertTrue(result is ExportQuality.Free720p)
    }

    @Test
    fun `paid tier BEST returns source resolution`() {
        val result = useCase.execute(isPaidUser = true, preset = ExportQualityPreset.BEST, sourceWidth = 1920, sourceHeight = 1080)
        val paid = result as ExportQuality.Paid
        assertEquals(ExportQualityPreset.BEST, paid.preset)
        assertEquals(1920, paid.sourceWidth)
        assertEquals(1080, paid.sourceHeight)
    }

    @Test
    fun `paid tier defaults to BEST when preset is null`() {
        val result = useCase.execute(isPaidUser = true, preset = null, sourceWidth = 1920, sourceHeight = 1080)
        val paid = result as ExportQuality.Paid
        assertEquals(ExportQualityPreset.BEST, paid.preset)
    }

    @Test
    fun `paid tier SMALL caps to 720p for 4K source`() {
        val result = useCase.execute(isPaidUser = true, preset = ExportQualityPreset.SMALL, sourceWidth = 3840, sourceHeight = 2160)
        val paid = result as ExportQuality.Paid
        assertEquals(ExportQualityPreset.SMALL, paid.preset)
        assertEquals(3840, paid.sourceWidth)
        assertEquals(2160, paid.sourceHeight)
    }

    @Test
    fun `paid tier BALANCED caps to 1080p for 4K source`() {
        val result = useCase.execute(isPaidUser = true, preset = ExportQualityPreset.BALANCED, sourceWidth = 3840, sourceHeight = 2160)
        val paid = result as ExportQuality.Paid
        assertEquals(ExportQualityPreset.BALANCED, paid.preset)
    }

    @Test
    fun `paid tier BALANCED uses source for 480p source`() {
        val result = useCase.execute(isPaidUser = true, preset = ExportQualityPreset.BALANCED, sourceWidth = 640, sourceHeight = 480)
        val paid = result as ExportQuality.Paid
        assertEquals(ExportQualityPreset.BALANCED, paid.preset)
        assertEquals(640, paid.sourceWidth)
        assertEquals(480, paid.sourceHeight)
    }
}