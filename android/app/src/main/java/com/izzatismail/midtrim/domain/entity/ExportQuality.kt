package com.izzatismail.midtrim.domain.entity

enum class ExportQualityPreset(
    val label: String,
    private val maxHeight: Int
) {
    SMALL("Small", 720),
    BALANCED("Balanced", 1080),
    BEST("Best", Int.MAX_VALUE);

    fun effectiveMaxHeight(sourceHeight: Int): Int =
        if (this == BEST) sourceHeight else minOf(maxHeight, sourceHeight)

    fun subtitle(sourceWidth: Int, sourceHeight: Int): String {
        val resText = if (this == BEST) "${sourceWidth}p" else "${effectiveMaxHeight(sourceHeight)}p"
        return when (this) {
            SMALL -> "Quick share · $resText"
            BALANCED -> "Good quality · $resText"
            BEST -> "Source quality · ${sourceWidth}p"
        }
    }

    companion object {
        fun availableFor(sourceHeight: Int): List<ExportQualityPreset> {
            val presets = mutableListOf(SMALL, BEST)
            if (sourceHeight > 1080) presets.add(1, BALANCED)
            return presets
        }
    }
}

sealed interface ExportQuality {
    data object Free720p : ExportQuality
    data class Paid(
        val preset: ExportQualityPreset,
        val sourceWidth: Int,
        val sourceHeight: Int
    ) : ExportQuality
}