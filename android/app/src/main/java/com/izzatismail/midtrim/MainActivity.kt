package com.izzatismail.midtrim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.entity.SourceVideoInfo
import com.izzatismail.midtrim.domain.entity.VideoMetadata
import com.izzatismail.midtrim.domain.repository.EntitlementCacheReader
import com.izzatismail.midtrim.domain.repository.ProjectRepository
import com.izzatismail.midtrim.domain.repository.VideoFileRepository
import com.izzatismail.midtrim.domain.repository.VideoMetadataService
import com.izzatismail.midtrim.domain.usecase.CalculateMergedDurationUseCase
import com.izzatismail.midtrim.domain.usecase.DeleteProjectUseCase
import com.izzatismail.midtrim.domain.usecase.FetchEntitlementStatusUseCase
import com.izzatismail.midtrim.domain.usecase.FetchProjectsUseCase
import com.izzatismail.midtrim.domain.usecase.ImportVideosUseCase
import com.izzatismail.midtrim.domain.usecase.RenameProjectUseCase
import com.izzatismail.midtrim.domain.usecase.ReorderVideosUseCase
import com.izzatismail.midtrim.domain.usecase.ValidateTrimDurationUseCase
import com.izzatismail.midtrim.presentation.navigation.Route
import com.izzatismail.midtrim.presentation.screens.*
import com.izzatismail.midtrim.presentation.viewmodel.ProjectListViewModel
import com.izzatismail.midtrim.presentation.viewmodel.VideoSelectionViewModel
import com.izzatismail.midtrim.ui.theme.MidTrimTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MidTrimTheme {
                MidTrimApp()
            }
        }
    }
}

@Composable
private fun MidTrimApp() {
    val navController = rememberNavController()

    val projectRepository = remember {
        object : ProjectRepository {
            override suspend fun fetchAllProjects(): List<ProjectInfo> = emptyList()
            override suspend fun fetchProject(id: String): ProjectInfo? = null
            override suspend fun save(project: ProjectInfo, sourceVideos: List<SourceVideoInfo>) = TODO("DI")
            override suspend fun delete(id: String) = TODO("DI")
            override suspend fun rename(id: String, name: String) = TODO("DI")
        }
    }
    val videoFileRepository = remember {
        object : VideoFileRepository {
            override suspend fun saveOutputVideo(sourceUri: String, targetFileName: String): String = TODO("DI")
            override suspend fun createDecryptedCopyForShare(uri: String): String = TODO("DI")
            override suspend fun createTempSegmentDir(): String = TODO("DI")
            override suspend fun cleanupTempSegments(dir: String) = TODO("DI")
            override suspend fun deleteOutputVideo(uri: String) = TODO("DI")
            override suspend fun deleteThumbnail(uri: String) = TODO("DI")
        }
    }
    val entitlementCacheReader = remember {
        object : EntitlementCacheReader {
            override val isPurchased: Boolean get() = false
            override val productId: String? get() = null
            override val lastVerifiedAt: Long? get() = null
        }
    }
    val videoMetadataService = remember {
        object : VideoMetadataService {
            override suspend fun fetchMetadata(videoUri: String): VideoMetadata = TODO("DI")
        }
    }

    val fetchProjectsUseCase = remember { FetchProjectsUseCase(repository = projectRepository) }
    val deleteProjectUseCase = remember { DeleteProjectUseCase(projectRepository = projectRepository, fileRepository = videoFileRepository) }
    val renameProjectUseCase = remember { RenameProjectUseCase(repository = projectRepository) }
    val fetchEntitlementStatusUseCase = remember { FetchEntitlementStatusUseCase(cache = entitlementCacheReader) }

    val projectListViewModel = remember {
        ProjectListViewModel(
            fetchProjectsUseCase = fetchProjectsUseCase,
            deleteProjectUseCase = deleteProjectUseCase,
            renameProjectUseCase = renameProjectUseCase,
            fetchEntitlementStatusUseCase = fetchEntitlementStatusUseCase
        )
    }

    val videoSelectionViewModel = remember {
        VideoSelectionViewModel(
            importVideosUseCase = ImportVideosUseCase(
                metadataService = videoMetadataService,
                fetchEntitlementStatus = fetchEntitlementStatusUseCase
            ),
            reorderVideosUseCase = ReorderVideosUseCase(),
            calculateMergedDurationUseCase = CalculateMergedDurationUseCase(),
            validateTrimDurationUseCase = ValidateTrimDurationUseCase(),
            fetchEntitlementStatusUseCase = fetchEntitlementStatusUseCase
        )
    }

    LaunchedEffect(Unit) {
        projectListViewModel.loadProjects()
    }

    NavHost(
        navController = navController,
        startDestination = Route.ProjectList.route
    ) {
        composable(Route.ProjectList.route) {
            val uiState by projectListViewModel.uiState.collectAsState()
            ProjectListScreen(
                uiState = uiState,
                onNewProject = { navController.navigate(Route.VideoSelection.route) },
                onDeleteProject = { projectListViewModel.deleteProject(it) },
                onRenameProject = { projectListViewModel.renameProject(it.id, "Renamed") },
                onUpgrade = { navController.navigate(Route.Paywall.route) },
                onProjectTap = {},
                onRefresh = { projectListViewModel.loadProjects() },
                onSettings = { navController.navigate(Route.HelpSettings.route) }
            )
        }
        composable(Route.VideoSelection.route) {
            LaunchedEffect(Unit) { videoSelectionViewModel.initialize() }
            val uiState by videoSelectionViewModel.uiState.collectAsState()
            VideoSelectionScreen(
                uiState = uiState,
                onAddVideos = { videoSelectionViewModel.importVideos(listOf("placeholder")) },
                onRemoveVideo = { videoSelectionViewModel.removeVideo(it) },
                onReorder = { from, to -> videoSelectionViewModel.reorder(from, to) },
                onDurationChange = { videoSelectionViewModel.setTrimDuration(it) },
                onContinue = { navController.navigate(Route.TrimDuration.route) },
                onBack = { navController.popBackStack() },
                onUpgrade = { navController.navigate(Route.Paywall.route) }
            )
        }
        composable(Route.TrimDuration.route) {
            val uiState by videoSelectionViewModel.uiState.collectAsState()
            TrimDurationScreen(
                selectedDuration = uiState.trimDuration,
                isPaidUser = uiState.isPaidUser,
                onDurationSelected = { videoSelectionViewModel.setTrimDuration(it) },
                onCustomTap = {
                    if (uiState.isPaidUser) {
                        videoSelectionViewModel.setTrimDuration(4.0)
                    } else {
                        navController.navigate(Route.Paywall.route)
                    }
                },
                onContinue = { navController.navigate(Route.NameProject.route) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.NameProject.route) {
            NameProjectScreen(
                defaultName = "Trim Project",
                onSave = { navController.navigate(Route.ProjectList.route) { popUpTo(Route.ProjectList.route) } },
                onDiscard = { navController.popBackStack(Route.ProjectList.route, false) }
            )
        }
        composable(Route.Paywall.route) {
            PaywallScreen(
                onPurchase = { },
                onRestore = { },
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(Route.HelpSettings.route) {
            HelpSettingsScreen(
                onRestore = { },
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}