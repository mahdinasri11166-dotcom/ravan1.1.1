package com.therapist.assistant.domain

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.Client

enum class AdvancedClientFilter {
    ALL, DEBTORS, ACTIVE, NEW_MONTH, INACTIVE_14D, ARCHIVED, HIGH_CANCEL
}

object ClientFilters {
    fun apply(
        clients: List<Client>,
        appointments: List<Appointment>,
        filter: AdvancedClientFilter,
        query: String,
        now: Long = System.currentTimeMillis()
    ): List<Client> {
        val q = query.trim()
        var list = clients
        if (q.isNotBlank()) {
            list = list.filter {
                it.name.contains(q, true) || it.phone.contains(q) ||
                    it.nationalId.contains(q) || it.notes.contains(q, true) ||
                    it.address.contains(q, true)
            }
        }
        val monthStart = now - 30L * 24 * 60 * 60 * 1000
        val day14 = 14L * 24 * 60 * 60 * 1000
        return when (filter) {
            AdvancedClientFilter.ALL -> list.filter { !it.isArchived }
            AdvancedClientFilter.ARCHIVED -> list.filter { it.isArchived }
            AdvancedClientFilter.DEBTORS -> list.filter { c ->
                appointments.any {
                    it.clientId == c.id && !it.isPaid && it.status != AppointmentStatus.CANCELLED &&
                        (it.fee - it.discount) > 0
                }
            }
            AdvancedClientFilter.ACTIVE -> list.filter { c ->
                !c.isArchived && appointments.any {
                    it.clientId == c.id && it.dateTimeMillis >= now - 60L * 24 * 60 * 60 * 1000
                }
            }
            AdvancedClientFilter.NEW_MONTH -> list.filter { !it.isArchived && it.createdAt >= monthStart }
            AdvancedClientFilter.INACTIVE_14D -> list.filter { c ->
                !c.isArchived && appointments.filter { it.clientId == c.id }
                    .maxOfOrNull { it.dateTimeMillis }?.let { now - it > day14 } != false &&
                    (appointments.none { it.clientId == c.id } ||
                        appointments.filter { it.clientId == c.id }.maxOf { it.dateTimeMillis }.let { now - it > day14 })
            }
            AdvancedClientFilter.HIGH_CANCEL -> list.filter { c ->
                val ap = appointments.filter { it.clientId == c.id }
                ap.size >= 3 && ap.count { it.status == AppointmentStatus.CANCELLED }.toFloat() / ap.size >= 0.4f
            }
        }
    }

    fun cancelRate(clientId: Long, appointments: List<Appointment>): Float {
        val ap = appointments.filter { it.clientId == clientId }
        if (ap.isEmpty()) return 0f
        return ap.count { it.status == AppointmentStatus.CANCELLED }.toFloat() / ap.size
    }
}
