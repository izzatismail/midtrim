package com.izzatismail.midtrim.data.repository

import com.izzatismail.midtrim.data.local.AppDatabase
import com.izzatismail.midtrim.data.local.toDomain
import com.izzatismail.midtrim.data.local.toEntity
import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.entity.SourceVideoInfo
import com.izzatismail.midtrim.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.first

class ProjectRepositoryImpl(
    private val database: AppDatabase
) : ProjectRepository {

    override suspend fun fetchAllProjects(): List<ProjectInfo> {
        return database.projectDao().getAllProjects().first().map { it.toDomain() }
    }

    override suspend fun fetchProject(id: String): ProjectInfo? {
        return database.projectDao().getProjectById(id)?.toDomain()
    }

    override suspend fun save(project: ProjectInfo, sourceVideos: List<SourceVideoInfo>) {
        database.projectDao().insert(project.toEntity())
        database.sourceVideoItemDao().insertAll(sourceVideos.map { it.toEntity() })
    }

    override suspend fun delete(id: String) {
        val entity = database.projectDao().getProjectById(id) ?: return
        database.projectDao().delete(entity)
    }

    override suspend fun rename(id: String, name: String) {
        val entity = database.projectDao().getProjectById(id) ?: return
        database.projectDao().update(entity.copy(name = name))
    }
}