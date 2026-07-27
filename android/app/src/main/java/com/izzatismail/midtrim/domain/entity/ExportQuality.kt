package com.izzatismail.midtrim.domain.entity

sealed interface ExportQuality {
    data object Free720p : ExportQuality
    data class PaidOriginal(val resolutionWidth: Int, val resolutionHeight: Int) : ExportQuality
}