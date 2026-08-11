package com.izzatismail.midtrim.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.ui.theme.Spacing
import com.izzatismail.midtrim.presentation.viewmodel.ProjectListUiState
import com.izzatismail.midtrim.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    uiState: ProjectListUiState,
    onNewProject: () -> Unit,
    onDeleteProject: (ProjectInfo) -> Unit,
    onRenameProject: (ProjectInfo) -> Unit,
    onUpgrade: () -> Unit,
    onProjectTap: (ProjectInfo) -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Text("...", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewProject,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", style = MaterialTheme.typography.headlineLarge)
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!uiState.isPaidUser) {
                UpgradeBanner(onUpgrade = onUpgrade)
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.projects.isEmpty() -> {
                    EmptyState(onNewProject = onNewProject)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        items(uiState.projects, key = { it.id }) { project ->
                            ProjectCard(
                                project = project,
                                onTap = { onProjectTap(project) },
                                onDelete = { onDeleteProject(project) },
                                onRename = { onRenameProject(project) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeBanner(onUpgrade: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
            .clickable(onClick = onUpgrade),
        color = PremiumAccent.copy(alpha = 0.15f),
        shape = RoundedCornerShape(Spacing.cornerButton)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Unlock custom trims, full quality & more →",
                style = MaterialTheme.typography.bodyMedium,
                color = PremiumAccent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EmptyState(onNewProject: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No projects yet",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "Create your first trim",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        Button(onClick = onNewProject) {
            Text("New Project")
        }
    }
}

@Composable
private fun ProjectCard(
    project: ProjectInfo,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cornerCard),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "${project.mergedDuration.toInt()}s · ${project.videoCount} videos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Text("···", style = MaterialTheme.typography.titleLarge)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}