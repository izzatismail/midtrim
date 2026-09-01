package com.izzatismail.midtrim.data.local

import com.izzatismail.midtrim.domain.entity.ExportQuality
import com.izzatismail.midtrim.domain.entity.ExportQualityPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportQualityMapperTest {

    @Test
    fun `free 720p to tier string`() {
        assertEquals("free_720p", ExportQuality.Free720p.toTierString())
    }

    @Test
    fun `paid 720p small to tier string`() {
        val quality = ExportQuality.Paid(ExportQualityPreset.SMALL, 1920, 1080)
        assertEquals("paid_720p", quality.toTierString())
    }

    @Test
    fun `paid 1080p balanced to tier string`() {
        val quality = ExportQuality.Paid(ExportQualityPreset.BALANCED, 3840, 2160)
        assertEquals("paid_1080p", quality.toTierString())
    }

    @Test
    fun `paid original best to tier string`() {
        val quality = ExportQuality.Paid(ExportQualityPreset.BEST, 1920, 1080)
        assertEquals("paid_original", quality.toTierString())
    }

    @Test
    fun `tier string free 720p to export quality`() {
        val quality = "free_720p".toExportQuality(1920, 1080)
        assertTrue(quality is ExportQuality.Free720p)
    }

    @Test
    fun `tier string paid 720p to export quality`() {
        val quality = "paid_720p".toExportQuality(1920, 1080)
        val paid = quality as ExportQuality.Paid
        assertEquals(ExportQualityPreset.SMALL, paid.preset)
        assertEquals(1920, paid.sourceWidth)
        assertEquals(1080, paid.sourceHeight)
    }

    @Test
    fun `tier string paid 1080p to export quality`() {
        val quality = "paid_1080p".toExportQuality(3840, 2160)
        val paid = quality as ExportQuality.Paid
        assertEquals(ExportQualityPreset.BALANCED, paid.preset)
        assertEquals(3840, paid.sourceWidth)
        assertEquals(2160, paid.sourceHeight)
    }

    @Test
    fun `tier string paid original to export quality`() {
        val quality = "paid_original".toExportQuality(1920, 1080)
        val paid = quality as ExportQuality.Paid
        assertEquals(ExportQualityPreset.BEST, paid.preset)
        assertEquals(1920, paid.sourceWidth)
        assertEquals(1080, paid.sourceHeight)
    }

    @Test
    fun `unknown tier string defaults to free 720p`() {
        val quality = "unknown_tier".toExportQuality(1920, 1080)
        assertTrue(quality is ExportQuality.Free720p)
    }

    @Test
    fun `round trip all tier strings`() {
        val testCases = listOf(
            Pair("free_720p", Pair(1920, 1080)),
            Pair("paid_720p", Pair(1920, 1080)),
            Pair("paid_1080p", Pair(3840, 2160)),
            Pair("paid_original", Pair(1920, 1080))
        )
        for ((tier, dims) in testCases) {
            val (w, h) = dims
            val quality = tier.toExportQuality(w, h)
            assertEquals(tier, quality.toTierString())
        }
    }
}