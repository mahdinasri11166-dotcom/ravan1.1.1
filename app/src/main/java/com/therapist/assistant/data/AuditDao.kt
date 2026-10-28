package com.therapist.assistant.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Insert
    suspend fun insert(log: AuditLog): Long

    @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC LIMIT :limit")
    fun recent(limit: Int = 100): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE entityType = :type AND entityId = :id ORDER BY createdAt DESC")
    fun forEntity(type: String, id: String): Flow<List<AuditLog>>
}

@Dao
interface FollowUpDao {
    @Query("SELECT * FROM follow_ups WHERE isDone = 0 ORDER BY dueMillis ASC")
    fun pending(): Flow<List<FollowUp>>

    @Insert
    suspend fun insert(f: FollowUp): Long

    @Update
    suspend fun update(f: FollowUp)

    @Query("UPDATE follow_ups SET isDone = 1 WHERE id = :id")
    suspend fun markDone(id: Long)
}
