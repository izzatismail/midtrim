package com.izzatismail.midtrim.domain.entity

data class TrimWindow(
    val startTime: Double,
    val endTime: Double
) {
    val duration: Double get() = endTime - startTime
}