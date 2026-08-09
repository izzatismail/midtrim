package com.izzatismail.midtrim.data.local

import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.entity.SourceVideoInfo

fun ProjectEntity.toDomain(): ProjectInfo = ProjectInfo(
    id = id,
    name = name,
    trimDuration = trimDuration,
    wasCustomDuration = wasCustomDuration,
    outputVideoUri = outputVideoUri,
    thumbnailUri = thumbnailUri,
    exportQualityTier = exportQualityTier,
    mergedDuration = mergedDuration,
    videoCount = videoCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ProjectInfo.toEntity(): ProjectEntity = ProjectEntity(
    id = id,
    name = name,
    trimDuration = trimDuration,
    wasCustomDuration = wasCustomDuration,
    outputVideoUri = outputVideoUri,
    thumbnailUri = thumbnailUri,
    exportQualityTier = exportQualityTier,
    mergedDuration = mergedDuration,
    videoCount = videoCount,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SourceVideoItemEntity.toDomain(): SourceVideoInfo = SourceVideoInfo(
    id = id,
    projectId = projectId,
    sourceVideoUri = sourceVideoUri,
    sourceVideoDuration = sourceVideoDuration,
    sourceFileSize = sourceFileSize,
    orderIndex = orderIndex,
    trimStartTime = trimStartTime,
    trimEndTime = trimEndTime
)

fun SourceVideoInfo.toEntity(): SourceVideoItemEntity = SourceVideoItemEntity(
    id = id,
    projectId = projectId,
    sourceVideoUri = sourceVideoUri,
    sourceVideoDuration = sourceVideoDuration,
    sourceFileSize = sourceFileSize,
    orderIndex = orderIndex,
    trimStartTime = trimStartTime,
    trimEndTime = trimEndTime
)