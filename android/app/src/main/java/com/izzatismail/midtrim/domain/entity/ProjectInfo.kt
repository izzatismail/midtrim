package com.izzatismail.midtrim.domain.entity

data class ProjectInfo(
    val id: String,
    val name: String,
    val trimDuration: Double,
    val wasCustomDuration: Boolean,
    val outputVideoUri: String,
    val thumbnailUri: String,
    val exportQualityTier: String,
    val mergedDuration: Double,
    val videoCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

data class SourceVideoInfo(
    val id: String,
    val projectId: String,
    val sourceVideoUri: String,
    val sourceVideoDuration: Double,
    val sourceFileSize: Long?,
    val orderIndex: Int,
    val trimStartTime: Double,
    val trimEndTime: Double
)