package com.therapist.assistant.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkshopDao {
    @Query("SELECT * FROM workshops ORDER BY dateMillis DESC")
    fun getAll(): Flow<List<Workshop>>

    @Query("SELECT * FROM workshops WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Workshop?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(w: Workshop): Long

    @Update
    suspend fun update(w: Workshop)

    @Delete
    suspend fun delete(w: Workshop)

    @Query("SELECT * FROM workshop_participants WHERE workshopId = :workshopId ORDER BY id ASC")
    suspend fun getParticipants(workshopId: Long): List<WorkshopParticipant>

    @Query("SELECT * FROM workshop_participants WHERE workshopId = :workshopId ORDER BY id ASC")
    fun observeParticipants(workshopId: Long): Flow<List<WorkshopParticipant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(p: WorkshopParticipant): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(list: List<WorkshopParticipant>)

    @Query("DELETE FROM workshop_participants WHERE workshopId = :workshopId")
    suspend fun clearParticipants(workshopId: Long)

    @Query("SELECT * FROM workshop_participants WHERE clientId = :clientId")
    suspend fun getParticipantsByClient(clientId: Long): List<WorkshopParticipant>

    @Delete
    suspend fun deleteParticipant(p: WorkshopParticipant)

    @Query("SELECT * FROM workshop_participants")
    suspend fun getAllParticipantsOnce(): List<WorkshopParticipant>

    @Query("SELECT * FROM workshops")
    suspend fun getAllOnce(): List<Workshop>
}
