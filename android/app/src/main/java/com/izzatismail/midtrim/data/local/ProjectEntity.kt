package com.izzatismail.midtrim.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
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