package com.izzatismail.midtrim.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.izzatismail.midtrim.domain.entity.ExportQualityPreset
import com.izzatismail.midtrim.presentation.viewmodel.VideoSelectionViewModel
import com.izzatismail.midtrim.ui.theme.Spacing
import com.izzatismail.midtrim.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrimDurationScreen(
    selectedDuration: Double,
    isPaidUser: Boolean,
    isCustomSelected: Boolean,
    customDuration: Double,
    availableQualityPresets: List<ExportQualityPreset>,
    selectedQualityPreset: ExportQualityPreset,
    sourceWidth: Int,
    sourceHeight: Int,
    onDurationSelected: (Double) -> Unit,
    onCustomTap: () -> Unit,
    onCustomDurationChanged: (Double) -> Unit,
    onQualityPresetSelected: (ExportQualityPreset) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durations = listOf(1.0, 2.0, 3.0)
    val isCustomOptionSelected = isCustomSelected && isPaidUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trim Duration") },
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
                .padding(horizontal = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Select trim duration",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                durations.forEach { duration ->
                    FilterChip(
                        selected = selectedDuration == duration,
                        onClick = { onDurationSelected(duration) },
                        label = {
                            Text("${duration.toInt()}s")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Surface(
                onClick = onCustomTap,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = if (isPaidUser)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isCustomOptionSelected) "Custom  ·  ${"%.1f".format(customDuration)}s" else "Custom",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isPaidUser) MaterialTheme.colorScheme.onSurface else PremiumAccent
                        )
                        if (!isPaidUser) {
                            Text(
                                text = " \uD83D\uDD12",
                                style = MaterialTheme.typography.bodyLarge,
                                color = PremiumAccent
                            )
                        }
                    }

                    if (isCustomOptionSelected) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        CustomDurationStepper(
                            value = customDuration,
                            onValueChanged = onCustomDurationChanged
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            ExportQualitySelector(
                isPaidUser = isPaidUser,
                availablePresets = availableQualityPresets,
                selectedPreset = selectedQualityPreset,
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
                onPresetSelected = onQualityPresetSelected,
                onUpgrade = onCustomTap
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Preview Trim")
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun ExportQualitySelector(
    isPaidUser: Boolean,
    availablePresets: List<ExportQualityPreset>,
    selectedPreset: ExportQualityPreset,
    sourceWidth: Int,
    sourceHeight: Int,
    onPresetSelected: (ExportQualityPreset) -> Unit,
    onUpgrade: () -> Unit
) {
    if (isPaidUser) {
        Text(
            text = "Export Quality",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            availablePresets.forEach { preset ->
                val isSelected = preset == selectedPreset
                Surface(
                    onClick = { onPresetSelected(preset) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = preset.subtitle(sourceWidth, sourceHeight),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    } else {
        Surface(
            onClick = onUpgrade,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "720p",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PremiumAccent
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "\uD83D\uDD12",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PremiumAccent
                )
            }
        }
    }
}

@Composable
private fun CustomDurationStepper(
    value: Double,
    onValueChanged: (Double) -> Unit
) {
    val min = VideoSelectionViewModel.CUSTOM_DURATION_MIN
    val max = VideoSelectionViewModel.CUSTOM_DURATION_MAX
    val step = VideoSelectionViewModel.CUSTOM_DURATION_STEP

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FilledIconButton(
            onClick = { onValueChanged(value - step) },
            enabled = value - step >= min - 0.001
        ) {
            Text("-", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "%.1fs".format(value),
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(Spacing.md))

        FilledIconButton(
            onClick = { onValueChanged(value + step) },
            enabled = value + step <= max + 0.001
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium)
        }
    }
}