package com.therapist.assistant.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework_tasks WHERE isDone = 0 ORDER BY dueMillis ASC")
    fun pending(): Flow<List<HomeworkTask>>

    @Query("SELECT * FROM homework_tasks WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun forClient(clientId: Long): Flow<List<HomeworkTask>>

    @Insert
    suspend fun insert(h: HomeworkTask): Long

    @Query("UPDATE homework_tasks SET isDone = 1 WHERE id = :id")
    suspend fun markDone(id: Long)
}