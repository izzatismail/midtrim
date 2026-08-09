package com.izzatismail.midtrim.domain.repository

interface VideoFileRepository {
    suspend fun saveOutputVideo(sourceUri: String, targetFileName: String): String
    suspend fun createDecryptedCopyForShare(uri: String): String
    suspend fun createTempSegmentDir(): String
    suspend fun cleanupTempSegments(dir: String)
    suspend fun deleteOutputVideo(uri: String)
    suspend fun deleteThumbnail(uri: String)
}