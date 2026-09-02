package com.izzatismail.midtrim.presentation.viewmodel

import com.izzatismail.midtrim.domain.usecase.CalculateMergedDurationUseCase
import com.izzatismail.midtrim.domain.usecase.FetchEntitlementStatusUseCase
import com.izzatismail.midtrim.domain.usecase.ImportVideosUseCase
import com.izzatismail.midtrim.domain.usecase.ReorderVideosUseCase
import com.izzatismail.midtrim.domain.usecase.ValidateTrimDurationUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VideoSelectionViewModelTest {

    private lateinit var viewModel: VideoSelectionViewModel

    @Before
    fun setUp() {
        viewModel = VideoSelectionViewModel(
            importVideosUseCase = ImportVideosUseCase(
                metadataService = FakeVideoMetadataService(),
                fetchEntitlementStatus = FetchEntitlementStatusUseCase(PrivateEntitlementCache(false))
            ),
            reorderVideosUseCase = ReorderVideosUseCase(),
            calculateMergedDurationUseCase = CalculateMergedDurationUseCase(),
            validateTrimDurationUseCase = ValidateTrimDurationUseCase(),
            fetchEntitlementStatusUseCase = FetchEntitlementStatusUseCase(PrivateEntitlementCache(true))
        )
    }

    @Test
    fun `selectCustomMode sets custom flags`() {
        viewModel.selectCustomMode()

        val state = viewModel.uiState.value
        assertTrue(state.isCustomDurationSelected)
        assertTrue(state.wasCustomDuration)
        assertEquals(VideoSelectionViewModel.DEFAULT_CUSTOM_DURATION, state.trimDuration, 0.001)
        assertEquals(VideoSelectionViewModel.DEFAULT_CUSTOM_DURATION, state.customDurationValue, 0.001)
    }

    @Test
    fun `setTrimDuration resets custom flags`() {
        viewModel.selectCustomMode()
        viewModel.setTrimDuration(2.0)

        val state = viewModel.uiState.value
        assertEquals(2.0, state.trimDuration, 0.001)
        assertFalse(state.isCustomDurationSelected)
        assertFalse(state.wasCustomDuration)
    }

    @Test
    fun `selectCustomMode recalculates merged duration`() {
        viewModel.setTrimDuration(2.0)
        viewModel.selectCustomMode()

        val state = viewModel.uiState.value
        val expectedMerged = CalculateMergedDurationUseCase().execute(
            VideoSelectionViewModel.DEFAULT_CUSTOM_DURATION, 0
        )
        assertEquals(expectedMerged, state.mergedDuration, 0.001)
    }
}

/**
 * Minimal entitlement cache for test — only supports reading a preset value.
 */
private class PrivateEntitlementCache(
    purchased: Boolean
) : com.izzatismail.midtrim.domain.repository.EntitlementCacheReader {
    override val isPurchased: Boolean = purchased
    override val productId: String? = null
    override val lastVerifiedAt: Long? = null
}

/**
 * Stub metadata service returning placeholder values.
 */
private class FakeVideoMetadataService : com.izzatismail.midtrim.domain.repository.VideoMetadataService {
    override suspend fun fetchMetadata(videoUri: String): com.izzatismail.midtrim.domain.entity.VideoMetadata {
        return com.izzatismail.midtrim.domain.entity.VideoMetadata(
            uri = videoUri,
            duration = 10.0,
            resolutionWidth = 1920,
            resolutionHeight = 1080,
            fileSize = 1_000_000L,
            format = "mp4"
        )
    }
}