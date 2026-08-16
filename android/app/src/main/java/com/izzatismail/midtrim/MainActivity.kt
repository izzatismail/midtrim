package com.izzatismail.midtrim

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.izzatismail.midtrim.data.billing.PlayBillingServiceImpl
import com.izzatismail.midtrim.data.local.AppDatabase
import com.izzatismail.midtrim.data.repository.EncryptedSharedPrefsEntitlementCache
import com.izzatismail.midtrim.data.repository.ProjectRepositoryImpl
import com.izzatismail.midtrim.data.repository.VideoFileRepositoryImpl
import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.entity.SourceVideoInfo
import com.izzatismail.midtrim.domain.entity.VideoMetadata
import com.izzatismail.midtrim.domain.repository.*
import com.izzatismail.midtrim.domain.usecase.*
import com.izzatismail.midtrim.presentation.navigation.Route
import com.izzatismail.midtrim.presentation.screens.*
import com.izzatismail.midtrim.presentation.viewmodel.ProjectListViewModel
import com.izzatismail.midtrim.presentation.viewmodel.VideoSelectionViewModel
import com.izzatismail.midtrim.ui.theme.MidTrimTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val activity = this
        setContent {
            MidTrimTheme {
                MidTrimApp(activity = activity)
            }
        }
    }
}

@Composable
private fun MidTrimApp(activity: MainActivity) {
    val navController = rememberNavController()
    var projectToRename by remember { mutableStateOf<ProjectInfo?>(null) }
    var renameText by remember { mutableStateOf("") }
    var paywallIsLoading by remember { mutableStateOf(false) }
    var paywallError by remember { mutableStateOf<String?>(null) }
    var restoreResult by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getInstance(context) }

    val projectRepository = remember { ProjectRepositoryImpl(database) }
    val videoFileRepository = remember { VideoFileRepositoryImpl(context) }
    val entitlementCacheWriter = remember { EncryptedSharedPrefsEntitlementCache(context) }

    val videoMetadataService = remember {
        object : VideoMetadataService {
            override suspend fun fetchMetadata(videoUri: String): VideoMetadata {
                return VideoMetadata(
                    uri = videoUri,
                    duration = 10.0,
                    resolutionWidth = 1920,
                    resolutionHeight = 1080,
                    fileSize = 1_000_000L,
                    format = "mp4"
                )
            }
        }
    }

    val playBillingService = remember {
        PlayBillingServiceImpl(
            context = context,
            activityProvider = { activity }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            playBillingService.endConnection()
        }
    }

    val purchaseEntitlementUseCase = remember {
        PurchaseEntitlementUseCase(billingService = playBillingService, cache = entitlementCacheWriter)
    }

    val fetchProjectsUseCase = remember { FetchProjectsUseCase(repository = projectRepository) }
    val deleteProjectUseCase = remember { DeleteProjectUseCase(projectRepository = projectRepository, fileRepository = videoFileRepository) }
    val renameProjectUseCase = remember { RenameProjectUseCase(repository = projectRepository) }
    val fetchEntitlementStatusUseCase = remember { FetchEntitlementStatusUseCase(cache = entitlementCacheWriter) }
    val restoreEntitlementUseCase = remember { RestoreEntitlementUseCase(billingService = playBillingService, cache = entitlementCacheWriter) }

    LaunchedEffect(Unit) {
        runCatching { restoreEntitlementUseCase.execute() }
    }

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

    projectToRename?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            title = { Text("Rename Project") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    label = { Text("Project name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        projectListViewModel.renameProject(project.id, renameText)
                        projectToRename = null
                    },
                    enabled = renameText.isNotBlank()
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("Cancel")
                }
            }
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
                onRenameProject = {
                    projectToRename = it
                    renameText = it.name
                },
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
                onPurchase = {
                    paywallError = null
                    paywallIsLoading = true
                    scope.launch {
                        when (val result = purchaseEntitlementUseCase.execute()) {
                            is PurchaseResult.Success -> {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                projectListViewModel.loadProjects()
                                navController.popBackStack()
                            }
                            is PurchaseResult.Cancelled -> {
                                paywallError = "Purchase cancelled"
                            }
                            is PurchaseResult.Failed -> {
                                paywallError = result.message
                            }
                        }
                        paywallIsLoading = false
                    }
                },
                onRestore = {
                    paywallError = null
                    paywallIsLoading = true
                    scope.launch {
                        val result = restoreEntitlementUseCase.execute()
                        if (result) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            projectListViewModel.loadProjects()
                            navController.popBackStack()
                        } else {
                            paywallError = "No purchases found to restore"
                        }
                        paywallIsLoading = false
                    }
                },
                onDismiss = { navController.popBackStack() },
                isLoading = paywallIsLoading,
                error = paywallError
            )
        }
        composable(Route.HelpSettings.route) {
            HelpSettingsScreen(
                onRestore = {
                    restoreResult = null
                    scope.launch {
                        val result = restoreEntitlementUseCase.execute()
                        restoreResult = if (result) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            "Purchases restored"
                        } else {
                            "No purchases found to restore"
                        }
                    }
                },
                onDismiss = { navController.popBackStack() },
                restoreResult = restoreResult,
                onLicenses = { navController.navigate(Route.Licenses.route) }
            )
        }
        composable(Route.Licenses.route) {
            LicensesScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}