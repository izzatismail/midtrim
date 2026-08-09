package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.entity.SourceVideoInfo
import com.izzatismail.midtrim.domain.repository.ProjectRepository
import com.izzatismail.midtrim.domain.repository.VideoFileRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchProjectsUseCaseTest {
    @Test
    fun `returns all projects`() = runBlocking {
        val repo = FakeProjectRepository()
        val useCase = FetchProjectsUseCase(repo)
        val result = useCase.execute()
        assertEquals(2, result.size)
    }

    @Test
    fun `empty database`() = runBlocking {
        val repo = FakeProjectRepository(emptyList())
        val useCase = FetchProjectsUseCase(repo)
        assertTrue(useCase.execute().isEmpty())
    }
}

class SaveProjectUseCaseTest {
    @Test
    fun `save success`() = runBlocking {
        val repo = FakeProjectRepository()
        val useCase = SaveProjectUseCase(repo)
        val project = makeProject("Test")
        useCase.execute(project, emptyList())
    }

    @Test
    fun `save empty name throws`() = runBlocking {
        val repo = FakeProjectRepository()
        val useCase = SaveProjectUseCase(repo)
        try {
            useCase.execute(makeProject(""), emptyList())
            throw AssertionError("Expected EmptyName")
        } catch (e: com.izzatismail.midtrim.domain.error.ProjectError.EmptyName) { }
    }
}

class DeleteProjectUseCaseTest {
    @Test
    fun `delete success`() = runBlocking {
        val fileRepo = FakeVideoFileRepository()
        val projectRepo = FakeProjectRepository()
        val useCase = DeleteProjectUseCase(projectRepo, fileRepo)
        useCase.execute(makeProject("Test"))
        assertTrue(fileRepo.didDeleteVideo)
        assertTrue(fileRepo.didDeleteThumbnail)
    }

    @Test
    fun `delete continues cascade on file failure`() = runBlocking {
        val fileRepo = FakeVideoFileRepository(shouldThrow = true)
        val projectRepo = FakeProjectRepository()
        val useCase = DeleteProjectUseCase(projectRepo, fileRepo)
        useCase.execute(makeProject("Test"))
        assertTrue(fileRepo.didDeleteVideo)
        assertTrue(fileRepo.didDeleteThumbnail)
        assertTrue(projectRepo.didDelete)
    }
}

class RenameProjectUseCaseTest {
    @Test
    fun `rename success`() = runBlocking {
        val repo = FakeProjectRepository()
        val useCase = RenameProjectUseCase(repo)
        useCase.execute("test-id", "Updated")
    }

    @Test
    fun `rename empty name throws`() = runBlocking {
        val repo = FakeProjectRepository()
        val useCase = RenameProjectUseCase(repo)
        try {
            useCase.execute("test-id", "")
            throw AssertionError("Expected EmptyName")
        } catch (e: com.izzatismail.midtrim.domain.error.ProjectError.EmptyName) { }
    }

    @Test
    fun `rename whitespace only throws`() = runBlocking {
        val repo = FakeProjectRepository()
        val useCase = RenameProjectUseCase(repo)
        try {
            useCase.execute("test-id", "   ")
            throw AssertionError("Expected EmptyName")
        } catch (e: com.izzatismail.midtrim.domain.error.ProjectError.EmptyName) { }
    }
}

private fun makeProject(name: String) = ProjectInfo(
    id = "test-id", name = name, trimDuration = 3.0, wasCustomDuration = false,
    outputVideoUri = "out.mp4", thumbnailUri = "thumb.jpg", exportQualityTier = "free_720p",
    mergedDuration = 3.0, videoCount = 1, createdAt = 0L, updatedAt = 0L
)

class FakeProjectRepository(
    private val projects: List<ProjectInfo>? = null
) : ProjectRepository {
    var didDelete = false

    private val stored = projects ?: listOf(
        makeProject("Project B").copy(id = "b", createdAt = 2000L),
        makeProject("Project A").copy(id = "a", createdAt = 1000L)
    )

    override suspend fun fetchAllProjects(): List<ProjectInfo> = stored
    override suspend fun fetchProject(id: String): ProjectInfo? = stored.find { it.id == id }
    override suspend fun save(project: ProjectInfo, sourceVideos: List<SourceVideoInfo>) = Unit
    override suspend fun delete(id: String) { didDelete = true }
    override suspend fun rename(id: String, name: String) = Unit
}

class FakeVideoFileRepository(private val shouldThrow: Boolean = false) : VideoFileRepository {
    var didDeleteVideo = false
    var didDeleteThumbnail = false

    override suspend fun saveOutputVideo(sourceUri: String, targetFileName: String): String {
        if (shouldThrow) throw Exception("Mock failure")
        return "/dev/null/$targetFileName"
    }

    override suspend fun createDecryptedCopyForShare(uri: String): String {
        if (shouldThrow) throw Exception("Mock failure")
        return "/dev/null/share_copy.mp4"
    }

    override suspend fun createTempSegmentDir(): String {
        if (shouldThrow) throw Exception("Mock failure")
        return "/dev/null/temp_segments"
    }

    override suspend fun cleanupTempSegments(dir: String) {
        if (shouldThrow) throw Exception("Mock failure")
    }

    override suspend fun deleteOutputVideo(uri: String) {
        didDeleteVideo = true
        if (shouldThrow) throw Exception("Mock failure")
    }

    override suspend fun deleteThumbnail(uri: String) {
        didDeleteThumbnail = true
        if (shouldThrow) throw Exception("Mock failure")
    }
}