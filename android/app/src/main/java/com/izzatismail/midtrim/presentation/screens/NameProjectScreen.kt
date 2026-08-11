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
fun NameProjectScreen(
    defaultName: String,
    onSave: (String) -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(defaultName) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Save Project") },
                navigationIcon = {
                    TextButton(onClick = onDiscard) { Text("Discard") }
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Button(
                onClick = { onSave(name) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}