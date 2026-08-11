package com.izzatismail.midtrim.presentation.viewmodel

import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.usecase.DeleteProjectUseCase
import com.izzatismail.midtrim.domain.usecase.FetchEntitlementStatusUseCase
import com.izzatismail.midtrim.domain.usecase.FetchProjectsUseCase
import com.izzatismail.midtrim.domain.usecase.RenameProjectUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProjectListUiState(
    val projects: List<ProjectInfo> = emptyList(),
    val isLoading: Boolean = true,
    val isPaidUser: Boolean = false,
    val error: String? = null
)

class ProjectListViewModel(
    private val fetchProjectsUseCase: FetchProjectsUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val renameProjectUseCase: RenameProjectUseCase,
    private val fetchEntitlementStatusUseCase: FetchEntitlementStatusUseCase
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    fun loadProjects() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val projects = fetchProjectsUseCase.execute()
                val isPaid = fetchEntitlementStatusUseCase.isPaidUser
                _uiState.value = ProjectListUiState(
                    projects = projects,
                    isPaidUser = isPaid,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load projects"
                )
            }
        }
    }

    fun deleteProject(project: ProjectInfo) {
        scope.launch {
            try {
                deleteProjectUseCase.execute(project)
                loadProjects()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete project"
                )
            }
        }
    }

    fun renameProject(projectId: String, newName: String) {
        scope.launch {
            try {
                renameProjectUseCase.execute(projectId, newName)
                loadProjects()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to rename project"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}