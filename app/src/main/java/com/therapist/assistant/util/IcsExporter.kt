package com.therapist.assistant.util

import com.therapist.assistant.data.Appointment
import java.text.SimpleDateFormat
import java.util.*

object IcsExporter {
    fun build(appointments: List<Appointment>, clinicName: String): String {
        val df = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("Asia/Tehran")
        }
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:-//GolSorkh//Therapist//FA")
        sb.appendLine("CALSCALE:GREGORIAN")
        appointments.forEach { a ->
            val start = df.format(Date(a.dateTimeMillis))
            val end = df.format(Date(a.dateTimeMillis + 50 * 60 * 1000))
            sb.appendLine("BEGIN:VEVENT")
            sb.appendLine("UID:${a.id}@golsorkh")
            sb.appendLine("DTSTART:$start")
            sb.appendLine("DTEND:$end")
            sb.appendLine("SUMMARY:جلسه با ${a.clientName} — $clinicName")
            if (a.notes.isNotBlank()) sb.appendLine("DESCRIPTION:${a.notes.replace("\n", "\\n")}")
            sb.appendLine("END:VEVENT")
        }
        sb.appendLine("END:VCALENDAR")
        return sb.toString()
    }
}
