package com.therapist.assistant.util

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import java.util.Calendar

/**
 * داشبورد سلامت کاری درمانگر: بار کاری، درآمد، الگوهای لغو.
 */
object TherapistWellness {

    data class WeekStats(
        val completedCount: Int,
        val cancelledCount: Int,
        val scheduledCount: Int,
        val incomePaid: Double,
        val unpaidTotal: Double,
        val avgSessionsPerDay: Double,
        val cancelRate: Double,
        val busiestDayLabel: String,
        val suggestionFa: String
    )

    fun weekStats(
        appointments: List<Appointment>,
        now: Long = System.currentTimeMillis()
    ): WeekStats {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.SATURDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        val weekStart = cal.timeInMillis
        val weekEnd = weekStart + 7L * 24 * 60 * 60 * 1000

        val week = appointments.filter { it.dateTimeMillis in weekStart until weekEnd }
        val completed = week.count { it.status == AppointmentStatus.COMPLETED }
        val cancelled = week.count { it.status == AppointmentStatus.CANCELLED }
        val scheduled = week.count { it.status == AppointmentStatus.SCHEDULED }
        val income = week.filter { it.isPaid && it.status != AppointmentStatus.CANCELLED }
            .sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
        val unpaid = week.filter { !it.isPaid && it.status != AppointmentStatus.CANCELLED }
            .sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
        val totalRelevant = (completed + cancelled).coerceAtLeast(1)
        val cancelRate = cancelled.toDouble() / totalRelevant
        val byDay = week.groupBy {
            Calendar.getInstance().apply { timeInMillis = it.dateTimeMillis }.get(Calendar.DAY_OF_WEEK)
        }
        val busiest = byDay.maxByOrNull { it.value.size }
        val dayNames = mapOf(
            Calendar.SATURDAY to "شنبه",
            Calendar.SUNDAY to "یکشنبه",
            Calendar.MONDAY to "دوشنبه",
            Calendar.TUESDAY to "سه‌شنبه",
            Calendar.WEDNESDAY to "چهارشنبه",
            Calendar.THURSDAY to "پنجشنبه",
            Calendar.FRIDAY to "جمعه"
        )
        val busiestLabel = busiest?.let { dayNames[it.key] ?: "—" } ?: "—"
        val avg = completed / 7.0
        val suggestion = when {
            cancelRate > 0.25 -> "نرخ لغو بالاست؛ یادآوری زودتر و پیش‌پرداخت را بررسی کنید."
            completed >= 25 -> "هفته پرکار — زمان خودمراقبتی و فاصله بین جلسات را حفظ کنید."
            completed <= 3 && scheduled <= 2 -> "ظرفیت خالی دارید؛ پیگیری مراجعان غیرفعال مفید است."
            unpaid > income && unpaid > 0 -> "بدهی‌های وصول‌نشده قابل توجه‌اند؛ پیگیری مالی توصیه می‌شود."
            else -> "بار کاری متعادل به‌نظر می‌رسد. ادامه دهید."
        }
        return WeekStats(
            completedCount = completed,
            cancelledCount = cancelled,
            scheduledCount = scheduled,
            incomePaid = income,
            unpaidTotal = unpaid,
            avgSessionsPerDay = avg,
            cancelRate = cancelRate,
            busiestDayLabel = busiestLabel,
            suggestionFa = suggestion
        )
    }
}
