package com.izzatismail.midtrim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SourceVideoItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SourceVideoItemEntity>)

    @Query("SELECT * FROM source_video_items WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getSourceVideoItemsByProjectId(projectId: String): List<SourceVideoItemEntity>

    @Query("DELETE FROM source_video_items WHERE projectId = :projectId")
    suspend fun deleteByProjectId(projectId: String)
}