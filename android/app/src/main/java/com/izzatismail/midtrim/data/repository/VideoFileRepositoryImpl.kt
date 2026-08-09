package com.izzatismail.midtrim.data.repository

import android.content.Context
import android.net.Uri
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.izzatismail.midtrim.domain.repository.VideoFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class VideoFileRepositoryImpl(
    private val context: Context
) : VideoFileRepository {

    override suspend fun saveOutputVideo(sourceUri: String, targetFileName: String): String =
        withContext(Dispatchers.IO) {
            val outputFile = File(context.filesDir, targetFileName)
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedFile.Builder(
                context,
                outputFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build().openFileOutput().use { encryptedStream ->
                openSourceStream(sourceUri).use { input ->
                    input.copyTo(encryptedStream)
                }
            }

            outputFile.absolutePath
        }

    private fun openSourceStream(sourceUri: String): InputStream {
        val uri = Uri.parse(sourceUri)
        return if (uri.scheme == "content") {
            context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("Cannot open content URI: $sourceUri")
        } else {
            File(sourceUri).inputStream()
        }
    }

    override suspend fun createDecryptedCopyForShare(uri: String): String =
        withContext(Dispatchers.IO) {
            val encryptedFile = File(uri)
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val decryptedFile = File(
                context.cacheDir,
                "share_${encryptedFile.nameWithoutExtension}_${System.currentTimeMillis()}.mp4"
            )

            EncryptedFile.Builder(
                context,
                encryptedFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build().openFileInput().use { encryptedStream ->
                decryptedFile.outputStream().use { output ->
                    encryptedStream.copyTo(output)
                }
            }

            decryptedFile.absolutePath
        }

    override suspend fun createTempSegmentDir(): String = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "trim_segments_${System.currentTimeMillis()}")
        dir.mkdirs()
        dir.absolutePath
    }

    override suspend fun cleanupTempSegments(dir: String) = withContext(Dispatchers.IO) {
        val directory = File(dir)
        if (directory.exists()) {
            directory.deleteRecursively()
        }
        Unit
    }

    override suspend fun deleteOutputVideo(uri: String) = withContext(Dispatchers.IO) {
        File(uri).delete()
        Unit
    }

    override suspend fun deleteThumbnail(uri: String) = withContext(Dispatchers.IO) {
        File(uri).delete()
        Unit
    }
}