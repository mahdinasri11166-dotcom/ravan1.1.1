package com.therapist.assistant

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.Client
import com.therapist.assistant.domain.AdvancedClientFilter
import com.therapist.assistant.domain.AppChangelog
import com.therapist.assistant.domain.ClientFilters
import com.therapist.assistant.domain.FinanceAnalytics
import com.therapist.assistant.domain.SmartReminder
import org.junit.Assert.*
import org.junit.Test

class DomainLogicTest {
    @Test
    fun financeUnpaidIgnoresCancelled() {
        val list = listOf(
            Appointment(clientId = 1, clientName = "a", dateTimeMillis = 1, fee = 100.0, discount = 10.0, isPaid = false),
            Appointment(clientId = 1, clientName = "a", dateTimeMillis = 2, fee = 50.0, isPaid = false, status = AppointmentStatus.CANCELLED)
        )
        assertEquals(90.0, FinanceAnalytics.unpaidTotal(list), 0.01)
    }

    @Test
    fun smartReminderHighCancel() {
        val ap = (1..5).map {
            Appointment(
                clientId = 9,
                clientName = "x",
                dateTimeMillis = it.toLong(),
                status = if (it <= 3) AppointmentStatus.CANCELLED else AppointmentStatus.COMPLETED
            )
        }
        val adv = SmartReminder.adviceForClient(9, ap)
        assertTrue(adv.highRisk)
        assertTrue(adv.cancelRate >= 0.4f)
        assertTrue(adv.suggestLeadMillis >= 24L * 60 * 60 * 1000)
    }

    @Test
    fun clientFilterDebtors() {
        val clients = listOf(Client(id = 1, name = "A"), Client(id = 2, name = "B"))
        val appts = listOf(
            Appointment(clientId = 1, clientName = "A", dateTimeMillis = 1, fee = 100.0, isPaid = false)
        )
        val r = ClientFilters.apply(clients, appts, AdvancedClientFilter.DEBTORS, "")
        assertEquals(1, r.size)
        assertEquals(1L, r[0].id)
    }

    @Test
    fun changelogHasCurrent() {
        assertTrue(AppChangelog.entries.isNotEmpty())
        assertNotNull(AppChangelog.forVersion("1.1.1"))
    }

    @Test
    fun invoiceSerialFormat() {
        val s = FinanceAnalytics.invoiceSerial("INV", "1403", 7)
        assertTrue(s.contains("INV"))
        assertTrue(s.contains("0007"))
    }
}
