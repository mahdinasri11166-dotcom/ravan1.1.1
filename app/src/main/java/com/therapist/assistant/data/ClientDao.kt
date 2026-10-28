package com.therapist.assistant.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Client?

    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: Long): Client?

    @Query("""
        SELECT * FROM clients 
        WHERE name LIKE '%' || :q || '%' 
           OR phone LIKE '%' || :q || '%'
           OR nationalId LIKE '%' || :q || '%'
           OR address LIKE '%' || :q || '%'
        ORDER BY name ASC
    """)
    fun searchClients(q: String): Flow<List<Client>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: Client): Long

    @Update
    suspend fun update(client: Client)

    @Delete
    suspend fun delete(client: Client)

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun getClientCount(): Int

    @Query("SELECT * FROM clients")
    suspend fun getAllClientsOnce(): List<Client>
}
