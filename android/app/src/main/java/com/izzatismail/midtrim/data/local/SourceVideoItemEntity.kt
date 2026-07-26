package com.izzatismail.midtrim.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "source_video_items",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId", "orderIndex"])]
)
data class SourceVideoItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val sourceVideoUri: String,
    val sourceVideoDuration: Double,
    val sourceFileSize: Long?,
    val orderIndex: Int,
    val trimStartTime: Double,
    val trimEndTime: Double
)