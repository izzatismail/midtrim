package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ExportQuality
import com.izzatismail.midtrim.domain.entity.ExportQualityPreset

class ResolveExportQualityUseCase {
    fun execute(
        isPaidUser: Boolean,
        preset: ExportQualityPreset?,
        sourceWidth: Int,
        sourceHeight: Int
    ): ExportQuality {
        if (isPaidUser) {
            return ExportQuality.Paid(preset ?: ExportQualityPreset.BEST, sourceWidth, sourceHeight)
        }
        return ExportQuality.Free720p
    }
}