package com.izzatismail.midtrim.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.izzatismail.midtrim.domain.entity.VideoMetadata
import com.izzatismail.midtrim.ui.theme.Spacing
import com.izzatismail.midtrim.presentation.viewmodel.VideoSelectionUiState
import com.izzatismail.midtrim.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoSelectionScreen(
    uiState: VideoSelectionUiState,
    onAddVideos: () -> Unit,
    onRemoveVideo: (Int) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onDurationChange: (Double) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Videos") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.md)
        ) {
            if (uiState.selectedVideos.isEmpty()) {
                EmptyVideoState(onAddVideos = onAddVideos)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(uiState.selectedVideos, key = { it.uri }) { video ->
                        val index = uiState.selectedVideos.indexOf(video)
                        VideoRow(
                            index = index,
                            video = video,
                            onRemove = { onRemoveVideo(index) }
                        )
                    }
                    item {
                        AddVideoButton(
                            isAtCap = !uiState.isPaidUser && uiState.selectedVideos.size >= 10,
                            onTap = if (!uiState.isPaidUser && uiState.selectedVideos.size >= 10)
                                onUpgrade else onAddVideos
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.sm))

                MergedDurationBanner(duration = uiState.mergedDuration)

                Spacer(modifier = Modifier.height(Spacing.md))

                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.selectedVideos.isNotEmpty()
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun EmptyVideoState(onAddVideos: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No videos selected",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Button(onClick = onAddVideos) {
            Text("Add Videos")
        }
    }
}

@Composable
private fun VideoRow(
    index: Int,
    video: VideoMetadata,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.uri.substringAfterLast("/"),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${video.duration.toInt()}s · ${video.format}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Text("✕", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun AddVideoButton(isAtCap: Boolean, onTap: () -> Unit) {
    Surface(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isAtCap) "+ Locked" else "+ Add Video",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isAtCap) PremiumAccent else MaterialTheme.colorScheme.onSurface
            )
            if (isAtCap) {
                Text(
                    text = " 🔒",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PremiumAccent
                )
            }
        }
    }
}

@Composable
private fun MergedDurationBanner(duration: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = "Merged length: ${duration.toInt()}s",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.sm)
        )
    }
}