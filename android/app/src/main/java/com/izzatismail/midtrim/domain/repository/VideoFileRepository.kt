package com.izzatismail.midtrim.domain.repository

interface VideoFileRepository {
    suspend fun deleteOutputVideo(uri: String)
    suspend fun deleteThumbnail(uri: String)
}