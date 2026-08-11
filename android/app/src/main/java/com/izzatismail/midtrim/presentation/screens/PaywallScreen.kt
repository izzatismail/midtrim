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
fun PaywallScreen(
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
                .padding(horizontal = Spacing.md.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Unlock MidTrim",
                style = MaterialTheme.typography.headlineLarge,
                color = PremiumAccent
            )

            Spacer(modifier = Modifier.height(Spacing.xl.dp))

            BenefitRow(text = "Custom trim duration (1–5s)")
            Spacer(modifier = Modifier.height(Spacing.md.dp))
            BenefitRow(text = "Full quality exports (up to 4K)")
            Spacer(modifier = Modifier.height(Spacing.md.dp))
            BenefitRow(text = "Up to 20 videos per project")

            Spacer(modifier = Modifier.height(Spacing.xl.dp))

            Text(
                text = "$5.00 — one-time purchase",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.lg.dp))

            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumAccent
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Unlock MidTrim")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm.dp))

            TextButton(onClick = onRestore) {
                Text("Restore Purchases")
            }

            error?.let {
                Spacer(modifier = Modifier.height(Spacing.sm.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.bodyLarge,
                color = PremiumAccent
            )
            Spacer(modifier = Modifier.width(Spacing.sm.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}