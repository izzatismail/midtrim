package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.error.ProjectError
import com.izzatismail.midtrim.domain.repository.ProjectRepository

class RenameProjectUseCase(
    private val repository: ProjectRepository
) {
    suspend fun execute(projectId: String, newName: String) {
        if (newName.isBlank()) {
            throw ProjectError.EmptyName
        }
        repository.rename(projectId, newName.trim())
    }
}