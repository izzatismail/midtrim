package com.izzatismail.midtrim.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val trimDuration: Double,
    val wasCustomDuration: Boolean,
    val exportQualityTier: String,
    val createdAt: Long,
    val outputVideoPath: String?,
    val thumbnailPath: String?
)
