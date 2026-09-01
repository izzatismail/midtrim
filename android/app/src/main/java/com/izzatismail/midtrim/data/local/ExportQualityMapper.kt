package com.izzatismail.midtrim.data.local

import com.izzatismail.midtrim.domain.entity.ExportQuality
import com.izzatismail.midtrim.domain.entity.ExportQualityPreset

fun ExportQuality.toTierString(): String = when (this) {
    is ExportQuality.Free720p -> "free_720p"
    is ExportQuality.Paid -> when (preset) {
        ExportQualityPreset.SMALL -> "paid_720p"
        ExportQualityPreset.BALANCED -> "paid_1080p"
        ExportQualityPreset.BEST -> "paid_original"
    }
}

fun String.toExportQuality(sourceWidth: Int, sourceHeight: Int): ExportQuality = when (this) {
    "free_720p" -> ExportQuality.Free720p
    "paid_720p" -> ExportQuality.Paid(ExportQualityPreset.SMALL, sourceWidth, sourceHeight)
    "paid_1080p" -> ExportQuality.Paid(ExportQualityPreset.BALANCED, sourceWidth, sourceHeight)
    "paid_original" -> ExportQuality.Paid(ExportQualityPreset.BEST, sourceWidth, sourceHeight)
    else -> ExportQuality.Free720p
}