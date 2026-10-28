package com.therapist.assistant.domain

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus

/**
 * یادآوری هوشمند بر اساس الگوی لغو مراجع.
 * اگر نرخ لغو بالا باشد، پیشنهاد می‌دهد یادآوری زودتر ارسال شود.
 */
object SmartReminder {
    data class Advice(
        val highRisk: Boolean,
        val cancelRate: Float,
        val messageFa: String,
        /** میلی‌ثانیه قبل از جلسه برای یادآوری پیشنهادی */
        val suggestLeadMillis: Long
    )

    fun adviceForClient(clientId: Long, appointments: List<Appointment>): Advice {
        val ap = appointments.filter { it.clientId == clientId }
        if (ap.size < 3) {
            return Advice(false, 0f, "داده کافی برای الگوی لغو نیست.", 24L * 60 * 60 * 1000)
        }
        val rate = ap.count { it.status == AppointmentStatus.CANCELLED }.toFloat() / ap.size
        return when {
            rate >= 0.4f -> Advice(
                true, rate,
                "نرخ لغو بالا (${(rate * 100).toInt()}٪) — یادآوری ۲ روز و ۱ روز قبل توصیه می‌شود.",
                48L * 60 * 60 * 1000
            )
            rate >= 0.25f -> Advice(
                true, rate,
                "نرخ لغو متوسط — یادآوری ۱ روز + ۱ ساعت قبل فعال بماند.",
                24L * 60 * 60 * 1000
            )
            else -> Advice(false, rate, "الگوی حضور پایدار است.", 24L * 60 * 60 * 1000)
        }
    }
}
