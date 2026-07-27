package com.izzatismail.midtrim.domain.repository

import com.izzatismail.midtrim.domain.entity.ProjectInfo
import com.izzatismail.midtrim.domain.entity.SourceVideoInfo

interface ProjectRepository {
    suspend fun fetchAllProjects(): List<ProjectInfo>
    suspend fun fetchProject(id: String): ProjectInfo?
    suspend fun save(project: ProjectInfo, sourceVideos: List<SourceVideoInfo>)
    suspend fun delete(id: String)
    suspend fun rename(id: String, name: String)
}