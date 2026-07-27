package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ExportQuality
import com.izzatismail.midtrim.domain.entity.VideoMetadata
import com.izzatismail.midtrim.domain.repository.FrameExtractor
import com.izzatismail.midtrim.domain.repository.VideoMetadataService
import com.izzatismail.midtrim.domain.repository.VideoTrimmer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportVideosUseCaseTest {
    @Test
    fun `import success`() = runBlocking {
        val service = FakeVideoMetadataService()
        val cache = FakeEntitlementCache(false)
        val useCase = ImportVideosUseCase(service, FetchEntitlementStatusUseCase(cache))
        val result = useCase.execute(listOf("v1.mp4", "v2.mp4"))
        assertEquals(2, result.size)
    }

    @Test
    fun `empty selection throws`() = runBlocking {
        val service = FakeVideoMetadataService()
        val cache = FakeEntitlementCache(false)
        val useCase = ImportVideosUseCase(service, FetchEntitlementStatusUseCase(cache))
        try {
            useCase.execute(emptyList())
            throw AssertionError("Expected EmptySelection")
        } catch (e: com.izzatismail.midtrim.domain.error.ImportVideoError.EmptySelection) { }
    }

    @Test
    fun `free tier cap enforced`() = runBlocking {
        val service = FakeVideoMetadataService()
        val cache = FakeEntitlementCache(false)
        val useCase = ImportVideosUseCase(service, FetchEntitlementStatusUseCase(cache))
        try {
            useCase.execute(List(11) { "v.mp4" })
            throw AssertionError("Expected ExceedsTierCap")
        } catch (e: com.izzatismail.midtrim.domain.error.ImportVideoError.ExceedsTierCap) {
            assertEquals(10, e.max)
            assertEquals(11, e.attempted)
        }
    }

    @Test
    fun `paid tier allows 20`() = runBlocking {
        val service = FakeVideoMetadataService()
        val cache = FakeEntitlementCache(true)
        val useCase = ImportVideosUseCase(service, FetchEntitlementStatusUseCase(cache))
        val result = useCase.execute(List(20) { "v.mp4" })
        assertEquals(20, result.size)
    }

    @Test
    fun `paid tier exceeds 21 throws`() = runBlocking {
        val service = FakeVideoMetadataService()
        val cache = FakeEntitlementCache(true)
        val useCase = ImportVideosUseCase(service, FetchEntitlementStatusUseCase(cache))
        try {
            useCase.execute(List(21) { "v.mp4" })
            throw AssertionError("Expected ExceedsTierCap")
        } catch (e: com.izzatismail.midtrim.domain.error.ImportVideoError.ExceedsTierCap) {
            assertEquals(20, e.max)
            assertEquals(21, e.attempted)
        }
    }
}

class TrimVideoUseCaseTest {
    @Test
    fun `trim success`() = runBlocking {
        val trimmer = FakeVideoTrimmer()
        val useCase = TrimVideoUseCase(trimmer, ValidateTrimDurationUseCase(), CalculateTrimWindowUseCase())
        val result = useCase.execute("v.mp4", 10.0, 3.0, false)
        assertEquals("trimmed_v.mp4", result)
    }

    @Test
    fun `rejected duration throws`() = runBlocking {
        val trimmer = FakeVideoTrimmer()
        val useCase = TrimVideoUseCase(trimmer, ValidateTrimDurationUseCase(), CalculateTrimWindowUseCase())
        try {
            useCase.execute("v.mp4", 10.0, 4.0, false)
            throw AssertionError("Expected FreeTierRejectsCustomDuration")
        } catch (e: com.izzatismail.midtrim.domain.error.ValidateTrimDurationError.FreeTierRejectsCustomDuration) { }
    }

    @Test
    fun `video shorter than trim throws`() = runBlocking {
        val trimmer = FakeVideoTrimmer()
        val useCase = TrimVideoUseCase(trimmer, ValidateTrimDurationUseCase(), CalculateTrimWindowUseCase())
        try {
            useCase.execute("v.mp4", 2.0, 5.0, true)
            throw AssertionError("Expected VideoShorterThanTrimDuration")
        } catch (e: com.izzatismail.midtrim.domain.error.TrimWindowError.VideoShorterThanTrimDuration) { }
    }

    @Test
    fun `empty source uri throws`() = runBlocking {
        val trimmer = FakeVideoTrimmer()
        val useCase = TrimVideoUseCase(trimmer, ValidateTrimDurationUseCase(), CalculateTrimWindowUseCase())
        try {
            useCase.execute("", 10.0, 3.0, false)
            throw AssertionError("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }
}

class MergeVideoSegmentsUseCaseTest {
    @Test
    fun `merge success`() = runBlocking {
        val trimmer = FakeVideoTrimmer()
        val useCase = MergeVideoSegmentsUseCase(trimmer, ResolveExportQualityUseCase())
        val result = useCase.execute(listOf("s1.mp4", "s2.mp4"), false, 1920, 1080)
        assertEquals("merged.mp4", result)
    }

    @Test
    fun `empty segments throws`() = runBlocking {
        val trimmer = FakeVideoTrimmer()
        val useCase = MergeVideoSegmentsUseCase(trimmer, ResolveExportQualityUseCase())
        try {
            useCase.execute(emptyList(), false, 1920, 1080)
            throw AssertionError("Expected MergeFailed")
        } catch (e: com.izzatismail.midtrim.domain.error.TrimError.MergeFailed) { }
    }
}

class GenerateThumbnailUseCaseTest {
    @Test
    fun `successful extraction`() = runBlocking {
        val extractor = FakeFrameExtractor(succeed = true)
        val useCase = GenerateThumbnailUseCase(extractor)
        val result = useCase.execute(videoUri = "merged.mp4")
        assertEquals("thumb_merged.mp4.jpg", result)
        assertEquals(0.0, extractor.lastExtractTime!!, 0.001)
    }

    @Test
    fun `extraction failure throws`() = runBlocking {
        val extractor = FakeFrameExtractor(succeed = false)
        val useCase = GenerateThumbnailUseCase(extractor)
        try {
            useCase.execute(videoUri = "merged.mp4")
            throw AssertionError("Expected ExtractionFailed")
        } catch (e: com.izzatismail.midtrim.domain.error.ThumbnailError.ExtractionFailed) {
            assertEquals(0.0, extractor.lastExtractTime!!, 0.001)
        }
    }
}

class FakeVideoMetadataService : VideoMetadataService {
    override suspend fun fetchMetadata(videoUri: String) = VideoMetadata(
        uri = videoUri, duration = 10.0,
        resolutionWidth = 1920, resolutionHeight = 1080,
        fileSize = 1_000_000, format = "mp4"
    )
}

class FakeVideoTrimmer : VideoTrimmer {
    override suspend fun trim(sourceUri: String, startTime: Double, endTime: Double) = "trimmed_$sourceUri"
    override suspend fun merge(segmentUris: List<String>, outputQuality: ExportQuality) = "merged.mp4"
}

class FakeFrameExtractor(private val succeed: Boolean) : FrameExtractor {
    var lastExtractTime: Double? = null

    override suspend fun extractFrame(videoUri: String, atTime: Double): String {
        lastExtractTime = atTime
        if (succeed) return "thumb_${videoUri}.jpg"
        throw com.izzatismail.midtrim.domain.error.ThumbnailError.ExtractionFailed("Mock failure")
    }
}