package com.therapist.assistant.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionnaireDao {
    @Query("SELECT * FROM questionnaires WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getForClient(clientId: Long): Flow<List<QuestionnaireResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: QuestionnaireResult): Long

    @Delete
    suspend fun delete(result: QuestionnaireResult)

    @Query("SELECT * FROM questionnaires")
    suspend fun getAllOnce(): List<QuestionnaireResult>
}
