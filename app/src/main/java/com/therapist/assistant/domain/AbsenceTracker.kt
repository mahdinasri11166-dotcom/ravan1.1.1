package com.therapist.assistant.domain

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.Client

object AbsenceTracker {
    data class AbsentClient(val client: Client, val lastTwoCancelled: Boolean, val lastSessionMillis: Long)

    /** مراجعی که ۲ نوبت آخرشان لغو/غایب بوده */
    fun consecutiveAbsences(
        clients: List<Client>,
        appointments: List<Appointment>,
        minStreak: Int = 2
    ): List<AbsentClient> {
        return clients.filter { !it.isArchived }.mapNotNull { c ->
            val recent = appointments
                .filter { it.clientId == c.id && it.status != AppointmentStatus.SCHEDULED }
                .sortedByDescending { it.dateTimeMillis }
                .take(minStreak)
            if (recent.size < minStreak) return@mapNotNull null
            val allCancel = recent.all { it.status == AppointmentStatus.CANCELLED }
            if (!allCancel) return@mapNotNull null
            AbsentClient(c, true, recent.first().dateTimeMillis)
        }.sortedByDescending { it.lastSessionMillis }
    }
}
