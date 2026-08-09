package com.izzatismail.midtrim.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.izzatismail.midtrim.data.local.AppDatabase
import com.izzatismail.midtrim.data.local.ProjectEntity
import com.izzatismail.midtrim.data.local.SourceVideoItemEntity
import com.izzatismail.midtrim.data.repository.ProjectRepositoryImpl
import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.entity.SourceVideoInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProjectRepositoryImplTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: ProjectRepositoryImpl

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java
        ).build()
        repository = ProjectRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and fetch all projects`() = runBlocking {
        repository.save(makeProject("Project A"), listOf(makeVideo("vid-1", "p-1")))
        repository.save(makeProject("Project B"), listOf(makeVideo("vid-2", "p-2")))

        val projects = repository.fetchAllProjects()
        assertEquals(2, projects.size)
    }

    @Test
    fun `fetch project by id`() = runBlocking {
        val project = makeProject("Test")
        repository.save(project, listOf(makeVideo("v-1", project.id)))

        val fetched = repository.fetchProject(project.id)
        assertNotNull(fetched)
        assertEquals("Test", fetched!!.name)
    }

    @Test
    fun `fetch non-existent project returns null`() = runBlocking {
        val result = repository.fetchProject("non-existent")
        assertNull(result)
    }

    @Test
    fun `delete project`() = runBlocking {
        val project = makeProject("To Delete")
        repository.save(project, listOf())
        assertEquals(1, repository.fetchAllProjects().size)

        repository.delete(project.id)
        assertEquals(0, repository.fetchAllProjects().size)
    }

    @Test
    fun `cascade delete removes source videos`() = runBlocking {
        val project = makeProject("Cascade")
        repository.save(project, listOf(makeVideo("v-1", project.id), makeVideo("v-2", project.id)))

        val videosBefore = database.projectDao().getVideosForProject(project.id)
        assertEquals(2, videosBefore.size)

        repository.delete(project.id)

        val videosAfter = database.projectDao().getVideosForProject(project.id)
        assertTrue(videosAfter.isEmpty())
    }

    @Test
    fun `rename project`() = runBlocking {
        val project = makeProject("Original")
        repository.save(project, listOf())

        repository.rename(project.id, "Renamed")
        val fetched = repository.fetchProject(project.id)
        assertEquals("Renamed", fetched!!.name)
    }

    @Test
    fun `ordering is createdAt descending`() = runBlocking {
        val a = makeProject("A").copy(id = "a", createdAt = 1000L)
        val b = makeProject("B").copy(id = "b", createdAt = 2000L)
        val c = makeProject("C").copy(id = "c", createdAt = 3000L)

        repository.save(c, listOf())
        repository.save(b, listOf())
        repository.save(a, listOf())

        val projects = repository.fetchAllProjects()
        assertEquals(listOf("C", "B", "A"), projects.map { it.name })
    }

    private fun makeProject(name: String) = ProjectInfo(
        id = java.util.UUID.randomUUID().toString(),
        name = name,
        trimDuration = 3.0,
        wasCustomDuration = false,
        outputVideoUri = "$name.mp4",
        thumbnailUri = "${name}_thumb.jpg",
        exportQualityTier = "free_720p",
        mergedDuration = 3.0,
        videoCount = 1,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun makeVideo(id: String, projectId: String) = SourceVideoInfo(
        id = id,
        projectId = projectId,
        sourceVideoUri = "source.mp4",
        sourceVideoDuration = 10.0,
        sourceFileSize = 1024L,
        orderIndex = 0,
        trimStartTime = 0.0,
        trimEndTime = 3.0
    )
}