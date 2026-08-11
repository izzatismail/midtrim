package com.izzatismail.midtrim.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izzatismail.midtrim.presentation.navigation.Spacing
import com.izzatismail.midtrim.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrimDurationScreen(
    selectedDuration: Double,
    isPaidUser: Boolean,
    onDurationSelected: (Double) -> Unit,
    onCustomTap: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durations = listOf(1.0, 2.0, 3.0)

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
                .padding(horizontal = Spacing.md.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.xl.dp))

            Text(
                text = "Select trim duration",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Spacing.lg.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm.dp)
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

            Spacer(modifier = Modifier.height(Spacing.sm.dp))

            Surface(
                onClick = if (isPaidUser) ({ onCustomTap() }) else onCustomTap,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = if (isPaidUser)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.md.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isPaidUser) "Custom" else "Custom",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isPaidUser) MaterialTheme.colorScheme.onSurface else PremiumAccent
                    )
                    if (!isPaidUser) {
                        Text(
                            text = " 🔒",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PremiumAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg.dp))

            QualityBadge(isPaidUser = isPaidUser, onUpgrade = onCustomTap)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Preview Trim")
            }

            Spacer(modifier = Modifier.height(Spacing.md.dp))
        }
    }
}

@Composable
private fun QualityBadge(isPaidUser: Boolean, onUpgrade: () -> Unit) {
    Surface(
        onClick = if (isPaidUser) ({}) else onUpgrade,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md.dp, vertical = Spacing.sm.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPaidUser) "Original Quality" else "720p",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPaidUser)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    PremiumAccent
            )
            if (!isPaidUser) {
                Spacer(modifier = Modifier.width(Spacing.xs.dp))
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PremiumAccent
                )
            }
        }
    }
}