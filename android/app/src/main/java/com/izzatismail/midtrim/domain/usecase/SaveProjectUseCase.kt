package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ExportQuality
import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.entity.SourceVideoInfo
import com.izzatismail.midtrim.domain.error.ProjectError
import com.izzatismail.midtrim.domain.repository.ProjectRepository

class SaveProjectUseCase(
    private val repository: ProjectRepository
) {
    suspend fun execute(project: ProjectInfo, sourceVideos: List<SourceVideoInfo>) {
        if (project.name.isBlank()) {
            throw ProjectError.EmptyName
        }
        val trimmed = project.copy(name = project.name.trim())
        repository.save(trimmed, sourceVideos)
    }
}