package com.izzatismail.midtrim.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.izzatismail.midtrim.data.repository.VideoFileRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class VideoFileRepositoryImplTest {
    private lateinit var repository: VideoFileRepositoryImpl
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = VideoFileRepositoryImpl(context)
    }

    @Test
    fun `create temp segment dir creates directory`() = runBlocking {
        val dir = repository.createTempSegmentDir()
        val file = File(dir)
        assertTrue(file.exists())
        assertTrue(file.isDirectory)
        file.deleteRecursively()
    }

    @Test
    fun `cleanup temp segments removes directory`() = runBlocking {
        val dir = repository.createTempSegmentDir()
        repository.cleanupTempSegments(dir)
        assertFalse(File(dir).exists())
    }

    @Test
    fun `cleanup non-existent dir does not throw`() = runBlocking {
        repository.cleanupTempSegments("/nonexistent/path")
    }

    @Test
    fun `delete non-existent file does not throw`() = runBlocking {
        repository.deleteOutputVideo("/nonexistent/file.mp4")
        repository.deleteThumbnail("/nonexistent/thumb.jpg")
    }

    @Test
    fun `save output video and delete it`() = runBlocking {
        val tempSource = File(context.cacheDir, "test_source_${System.currentTimeMillis()}.mp4")
        tempSource.writeBytes("fake video content".toByteArray())

        val path = repository.saveOutputVideo(tempSource.absolutePath, "test_output.mp4")
        val savedFile = File(path)
        assertTrue(savedFile.exists())

        repository.deleteOutputVideo(path)
        assertFalse(savedFile.exists())

        tempSource.delete()
    }

    @Test
    fun `saved output video is encrypted at rest`() = runBlocking {
        val originalContent = "secret video content".toByteArray()
        val tempSource = File(context.cacheDir, "plain_source_${System.currentTimeMillis()}.mp4")
        tempSource.writeBytes(originalContent)

        val path = repository.saveOutputVideo(tempSource.absolutePath, "encrypted_test.mp4")
        val savedFile = File(path)
        assertTrue(savedFile.exists())

        val savedBytes = savedFile.readBytes()
        assertFalse("Encrypted file must differ from plaintext input", savedBytes contentEquals originalContent)

        savedFile.delete()
        tempSource.delete()
    }
}