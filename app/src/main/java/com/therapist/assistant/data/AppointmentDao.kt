package com.therapist.assistant.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY dateTimeMillis ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE status = 'SCHEDULED' AND dateTimeMillis >= :now ORDER BY dateTimeMillis ASC")
    fun getUpcomingAppointments(now: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateTimeMillis >= :dayStart AND dateTimeMillis < :dayEnd ORDER BY dateTimeMillis ASC")
    fun getAppointmentsForDay(dayStart: Long, dayEnd: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE clientId = :clientId ORDER BY dateTimeMillis DESC")
    fun getAppointmentsForClient(clientId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Long): Appointment?

    @Query("SELECT * FROM appointments WHERE dateTimeMillis >= :start AND dateTimeMillis < :end ORDER BY dateTimeMillis ASC")
    fun getAppointmentsBetween(start: Long, end: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE isPaid = 0 AND status != 'CANCELLED' ORDER BY dateTimeMillis DESC")
    fun getUnpaidAppointments(): Flow<List<Appointment>>

    @Query("""
        SELECT * FROM appointments 
        WHERE clientName LIKE '%' || :q || '%' 
           OR notes LIKE '%' || :q || '%' 
           OR sessionNotes LIKE '%' || :q || '%'
           OR soapSubjective LIKE '%' || :q || '%'
           OR soapObjective LIKE '%' || :q || '%'
           OR soapAssessment LIKE '%' || :q || '%'
           OR soapPlan LIKE '%' || :q || '%'
           OR noteTags LIKE '%' || :q || '%'
           OR cancelReason LIKE '%' || :q || '%'
        ORDER BY dateTimeMillis DESC
    """)
    fun searchAppointments(q: String): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Appointment>)

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(fee - discount) FROM appointments WHERE isPaid = 1 AND dateTimeMillis >= :start AND dateTimeMillis < :end")
    suspend fun getPaidIncomeBetween(start: Long, end: Long): Double?

    @Query("SELECT SUM(fee - discount) FROM appointments WHERE isPaid = 0 AND status != 'CANCELLED'")
    suspend fun getTotalUnpaid(): Double?

    @Query("SELECT COUNT(*) FROM appointments WHERE status = 'SCHEDULED' AND dateTimeMillis >= :now")
    suspend fun getUpcomingCount(now: Long): Int

    @Query("SELECT * FROM appointments")
    suspend fun getAllOnce(): List<Appointment>

    @Query("SELECT COUNT(*) FROM appointments WHERE invoiceSerial != ''")
    suspend fun getInvoiceCount(): Int
}
