package com.izzatismail.midtrim.data

import com.izzatismail.midtrim.data.local.ProjectEntity
import com.izzatismail.midtrim.data.local.SourceVideoItemEntity
import com.izzatismail.midtrim.data.local.toDomain
import com.izzatismail.midtrim.data.local.toEntity
import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.entity.SourceVideoInfo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EntityMappersTest {

    @Test
    fun `project entity to domain round-trip`() {
        val entity = ProjectEntity(
            id = "test-id",
            name = "Test Project",
            trimDuration = 3.0,
            wasCustomDuration = false,
            outputVideoUri = "out.mp4",
            thumbnailUri = "thumb.jpg",
            exportQualityTier = "free_720p",
            mergedDuration = 6.0,
            videoCount = 2,
            createdAt = 1000L,
            updatedAt = 2000L
        )
        val domain = entity.toDomain()
        val backToEntity = domain.toEntity()

        assertEquals(entity.id, backToEntity.id)
        assertEquals(entity.name, backToEntity.name)
        assertEquals(entity.trimDuration, backToEntity.trimDuration, 0.001)
        assertEquals(entity.wasCustomDuration, backToEntity.wasCustomDuration)
        assertEquals(entity.outputVideoUri, backToEntity.outputVideoUri)
        assertEquals(entity.thumbnailUri, backToEntity.thumbnailUri)
        assertEquals(entity.exportQualityTier, backToEntity.exportQualityTier)
        assertEquals(entity.mergedDuration, backToEntity.mergedDuration, 0.001)
        assertEquals(entity.videoCount, backToEntity.videoCount)
        assertEquals(entity.createdAt, backToEntity.createdAt)
        assertEquals(entity.updatedAt, backToEntity.updatedAt)
    }

    @Test
    fun `source video item entity to domain round-trip`() {
        val entity = SourceVideoItemEntity(
            id = "vid-1",
            projectId = "project-1",
            sourceVideoUri = "source.mp4",
            sourceVideoDuration = 10.0,
            sourceFileSize = 1024L,
            orderIndex = 0,
            trimStartTime = 3.5,
            trimEndTime = 6.5
        )
        val domain = entity.toDomain()
        val backToEntity = domain.toEntity()

        assertEquals(entity.id, backToEntity.id)
        assertEquals(entity.projectId, backToEntity.projectId)
        assertEquals(entity.sourceVideoUri, backToEntity.sourceVideoUri)
        assertEquals(entity.sourceVideoDuration, backToEntity.sourceVideoDuration, 0.001)
        assertEquals(entity.sourceFileSize, backToEntity.sourceFileSize)
        assertEquals(entity.orderIndex, backToEntity.orderIndex)
        assertEquals(entity.trimStartTime, backToEntity.trimStartTime, 0.001)
        assertEquals(entity.trimEndTime, backToEntity.trimEndTime, 0.001)
    }

    @Test
    fun `source video item with null file size round-trip`() {
        val entity = SourceVideoItemEntity(
            id = "vid-2",
            projectId = "project-1",
            sourceVideoUri = "source.mp4",
            sourceVideoDuration = 5.0,
            sourceFileSize = null,
            orderIndex = 1,
            trimStartTime = 1.0,
            trimEndTime = 4.0
        )
        val domain = entity.toDomain()
        val backToEntity = domain.toEntity()

        assertEquals(entity.sourceFileSize, backToEntity.sourceFileSize)
        assertEquals(null, backToEntity.sourceFileSize)
    }

    @Test
    fun `domain to entity preserves id`() {
        val domain = ProjectInfo(
            id = "my-project",
            name = "My Project",
            trimDuration = 2.0,
            wasCustomDuration = false,
            outputVideoUri = "out.mp4",
            thumbnailUri = "thumb.jpg",
            exportQualityTier = "paid_original",
            mergedDuration = 4.0,
            videoCount = 2,
            createdAt = 3000L,
            updatedAt = 3000L
        )
        val entity = domain.toEntity()
        assertEquals("my-project", entity.id)
        assertEquals("paid_original", entity.exportQualityTier)
    }
}