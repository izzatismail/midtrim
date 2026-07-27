package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.repository.ProjectRepository

class FetchProjectsUseCase(
    private val repository: ProjectRepository
) {
    suspend fun execute(): List<ProjectInfo> {
        return repository.fetchAllProjects()
    }
}