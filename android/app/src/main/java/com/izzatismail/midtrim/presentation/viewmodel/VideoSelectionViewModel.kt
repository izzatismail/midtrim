package com.izzatismail.midtrim.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.izzatismail.midtrim.domain.entity.ExportQualityPreset
import com.izzatismail.midtrim.domain.entity.VideoMetadata
import com.izzatismail.midtrim.domain.usecase.CalculateMergedDurationUseCase
import com.izzatismail.midtrim.domain.usecase.FetchEntitlementStatusUseCase
import com.izzatismail.midtrim.domain.usecase.ImportVideosUseCase
import com.izzatismail.midtrim.domain.usecase.ReorderVideosUseCase
import com.izzatismail.midtrim.domain.usecase.ValidateTrimDurationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VideoSelectionUiState(
    val selectedVideos: List<VideoMetadata> = emptyList(),
    val trimDuration: Double = 1.0,
    val mergedDuration: Double = 0.0,
    val isPaidUser: Boolean = false,
    val isLoading: Boolean = false,
    val importError: String? = null,
    val selectedQualityPreset: ExportQualityPreset = ExportQualityPreset.BEST,
    val availableQualityPresets: List<ExportQualityPreset> = emptyList(),
    val sourceWidth: Int = 1920,
    val sourceHeight: Int = 1080,
    val isCustomDurationSelected: Boolean = false,
    val customDurationValue: Double = VideoSelectionViewModel.DEFAULT_CUSTOM_DURATION,
    val wasCustomDuration: Boolean = false
)

class VideoSelectionViewModel(
    private val importVideosUseCase: ImportVideosUseCase,
    private val reorderVideosUseCase: ReorderVideosUseCase,
    private val calculateMergedDurationUseCase: CalculateMergedDurationUseCase,
    private val validateTrimDurationUseCase: ValidateTrimDurationUseCase,
    private val fetchEntitlementStatusUseCase: FetchEntitlementStatusUseCase
) : ViewModel() {

    companion object {
        const val DEFAULT_CUSTOM_DURATION = 3.0
        const val CUSTOM_DURATION_MIN = 1.0
        const val CUSTOM_DURATION_MAX = 5.0
        const val CUSTOM_DURATION_STEP = 0.1
    }

    private val _uiState = MutableStateFlow(VideoSelectionUiState())
    val uiState: StateFlow<VideoSelectionUiState> = _uiState.asStateFlow()

    private val existingUris: Set<String>
        get() = _uiState.value.selectedVideos.map { it.uri }.toSet()

    fun initialize() {
        viewModelScope.launch {
            val isPaid = fetchEntitlementStatusUseCase.isPaidUser
            val videos = _uiState.value.selectedVideos
            val sourceWidth = videos.maxOfOrNull { it.resolutionWidth } ?: 1920
            val sourceHeight = videos.maxOfOrNull { it.resolutionHeight } ?: 1080
            _uiState.value = _uiState.value.copy(
                isPaidUser = isPaid,
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
                availableQualityPresets = ExportQualityPreset.availableFor(sourceHeight)
            )
        }
    }

    fun importVideos(videoUris: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, importError = null)
            try {
                val metadata = importVideosUseCase.execute(videoUris)
                val existing = existingUris
                val newMetadata = metadata.filter { it.uri !in existing }
                val all = _uiState.value.selectedVideos + newMetadata
                val merged = calculateMergedDurationUseCase.execute(
                    _uiState.value.trimDuration, all.size
                )
                val sourceWidth = all.maxOfOrNull { it.resolutionWidth } ?: 1920
                val sourceHeight = all.maxOfOrNull { it.resolutionHeight } ?: 1080
                _uiState.value = _uiState.value.copy(
                    selectedVideos = all,
                    mergedDuration = merged,
                    isLoading = false,
                    sourceWidth = sourceWidth,
                    sourceHeight = sourceHeight,
                    availableQualityPresets = ExportQualityPreset.availableFor(sourceHeight)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    importError = e.message
                )
            }
        }
    }

    fun removeVideo(index: Int) {
        val updated = _uiState.value.selectedVideos.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            val merged = calculateMergedDurationUseCase.execute(
                _uiState.value.trimDuration, updated.size
            )
            _uiState.value = _uiState.value.copy(
                selectedVideos = updated,
                mergedDuration = merged
            )
        }
    }

    fun reorder(fromIndex: Int, toIndex: Int) {
        val items = _uiState.value.selectedVideos.toMutableList()
        if (fromIndex in items.indices && toIndex in items.indices) {
            val item = items.removeAt(fromIndex)
            items.add(toIndex, item)
            _uiState.value = _uiState.value.copy(selectedVideos = items)
        }
    }

    fun setTrimDuration(duration: Double) {
        if (validateTrimDurationUseCase.isAllowed(duration, _uiState.value.isPaidUser)) {
            val merged = calculateMergedDurationUseCase.execute(
                duration, _uiState.value.selectedVideos.size
            )
            _uiState.value = _uiState.value.copy(
                trimDuration = duration,
                mergedDuration = merged,
                isCustomDurationSelected = false,
                wasCustomDuration = false
            )
        }
    }

    fun selectCustomMode() {
        setCustomDuration(DEFAULT_CUSTOM_DURATION)
        _uiState.value = _uiState.value.copy(isCustomDurationSelected = true)
    }

    fun setCustomDuration(value: Double) {
        val clamped = value.coerceIn(CUSTOM_DURATION_MIN, CUSTOM_DURATION_MAX)
        val rounded = kotlin.math.round(clamped / CUSTOM_DURATION_STEP) * CUSTOM_DURATION_STEP
        if (validateTrimDurationUseCase.isAllowed(rounded, _uiState.value.isPaidUser)) {
            val merged = calculateMergedDurationUseCase.execute(
                rounded, _uiState.value.selectedVideos.size
            )
            _uiState.value = _uiState.value.copy(
                trimDuration = rounded,
                mergedDuration = merged,
                customDurationValue = rounded,
                wasCustomDuration = true
            )
        }
    }

    fun setQualityPreset(preset: ExportQualityPreset) {
        _uiState.value = _uiState.value.copy(selectedQualityPreset = preset)
    }

    fun clearImportError() {
        _uiState.value = _uiState.value.copy(importError = null)
    }
}