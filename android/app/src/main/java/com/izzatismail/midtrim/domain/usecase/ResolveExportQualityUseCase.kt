package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ExportQuality

class ResolveExportQualityUseCase {
    fun execute(isPaidUser: Boolean, sourceWidth: Int, sourceHeight: Int): ExportQuality {
        if (isPaidUser) {
            return ExportQuality.PaidOriginal(sourceWidth, sourceHeight)
        }
        return ExportQuality.Free720p
    }
}