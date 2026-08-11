package com.izzatismail.midtrim.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izzatismail.midtrim.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSettingsScreen(
    appVersion: String = "1.0.0",
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
    restoreResult: String? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Settings") },
                navigationIcon = {
                    TextButton(onClick = onDismiss) { Text("Close") }
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onRestore,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Purchases")
            }

            restoreResult?.let {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Version $appVersion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}