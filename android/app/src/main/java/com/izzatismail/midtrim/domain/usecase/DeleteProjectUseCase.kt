package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.repository.ProjectRepository
import com.izzatismail.midtrim.domain.repository.VideoFileRepository

class DeleteProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val fileRepository: VideoFileRepository
) {
    suspend fun execute(project: ProjectInfo) {
        try { fileRepository.deleteOutputVideo(project.outputVideoUri) } catch (_: Exception) {}
        try { fileRepository.deleteThumbnail(project.thumbnailUri) } catch (_: Exception) {}
        projectRepository.delete(project.id)
    }
}