package com.therapist.assistant.data

import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val appointmentDao: AppointmentDao,
    private val clientDao: ClientDao,
    private val questionnaireDao: QuestionnaireDao
) {
    fun getAllAppointments() = appointmentDao.getAllAppointments()
    fun getUpcomingAppointments(now: Long = System.currentTimeMillis()) =
        appointmentDao.getUpcomingAppointments(now)
    fun getAppointmentsForDay(start: Long, end: Long) = appointmentDao.getAppointmentsForDay(start, end)
    fun getAppointmentsForClient(clientId: Long) = appointmentDao.getAppointmentsForClient(clientId)
    fun getAppointmentsBetween(start: Long, end: Long) = appointmentDao.getAppointmentsBetween(start, end)
    fun getUnpaidAppointments() = appointmentDao.getUnpaidAppointments()
    fun searchAppointments(q: String) = appointmentDao.searchAppointments(q)

    suspend fun getAppointmentById(id: Long) = appointmentDao.getAppointmentById(id)
    suspend fun insert(a: Appointment): Long = appointmentDao.insert(a)
    suspend fun insertAll(list: List<Appointment>) = appointmentDao.insertAll(list)
    suspend fun update(a: Appointment) = appointmentDao.update(a)
    suspend fun delete(a: Appointment) = appointmentDao.delete(a)
    suspend fun getPaidIncomeBetween(s: Long, e: Long) = appointmentDao.getPaidIncomeBetween(s, e) ?: 0.0
    suspend fun getTotalUnpaid() = appointmentDao.getTotalUnpaid() ?: 0.0
    suspend fun getAllAppointmentsOnce() = appointmentDao.getAllOnce()
    suspend fun getInvoiceCount() = appointmentDao.getInvoiceCount()

    fun getAllClients() = clientDao.getAllClients()
    fun searchClients(q: String) = clientDao.searchClients(q)
    suspend fun getClientById(id: Long) = clientDao.getClientById(id)
    suspend fun insertClient(c: Client): Long = clientDao.insert(c)
    suspend fun updateClient(c: Client) = clientDao.update(c)
    suspend fun deleteClient(c: Client) = clientDao.delete(c)
    suspend fun getClientCount() = clientDao.getClientCount()
    suspend fun getAllClientsOnce() = clientDao.getAllClientsOnce()

    fun getQuestionnaires(clientId: Long) = questionnaireDao.getForClient(clientId)
    suspend fun insertQuestionnaire(r: QuestionnaireResult) = questionnaireDao.insert(r)
    suspend fun getAllQuestionnairesOnce() = questionnaireDao.getAllOnce()
}
