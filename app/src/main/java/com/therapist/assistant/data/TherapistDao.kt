package com.therapist.assistant.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TherapistDao {
    @Query("SELECT * FROM therapists WHERE isActive = 1 ORDER BY name ASC")
    fun getActive(): Flow<List<Therapist>>

    @Query("SELECT * FROM therapists ORDER BY name ASC")
    fun getAll(): Flow<List<Therapist>>

    @Query("SELECT * FROM therapists WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Therapist?

    @Query("SELECT COUNT(*) FROM therapists")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: Therapist): Long

    @Update
    suspend fun update(t: Therapist)

    @Delete
    suspend fun delete(t: Therapist)
}
