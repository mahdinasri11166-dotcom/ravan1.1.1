package com.therapist.assistant.domain

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus

/** منطق مالی جدا از ViewModel */
object FinanceAnalytics {
    fun unpaidTotal(list: List<Appointment>): Double =
        list.filter { !it.isPaid && it.status != AppointmentStatus.CANCELLED }
            .sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }

    fun paidTotal(list: List<Appointment>): Double =
        list.filter { it.isPaid && it.status != AppointmentStatus.CANCELLED }
            .sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }

    fun invoiceSerial(prefix: String, yearFa: String, seq: Int): String =
        "%s-%s-%04d".format(prefix.ifBlank { "INV" }, yearFa, seq.coerceAtLeast(1))
}
