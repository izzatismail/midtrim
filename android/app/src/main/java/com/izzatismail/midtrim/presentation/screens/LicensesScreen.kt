package com.izzatismail.midtrim.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izzatismail.midtrim.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
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
                .padding(horizontal = Spacing.md)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Nunito Font",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "Copyright (c) 2014, Vernon Adams\n" +
                        "Copyright (c) 2014, Google Inc.\n\n" +
                        "Licensed under the SIL Open Font License, Version 1.1.\n" +
                        "This license is available with a FAQ at:\n" +
                        "https://scripts.sil.org/OFL",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
