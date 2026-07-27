package com.izzatismail.midtrim.domain.entity

data class VideoMetadata(
    val uri: String,
    val duration: Double,
    val resolutionWidth: Int,
    val resolutionHeight: Int,
    val fileSize: Long?,
    val format: String
)