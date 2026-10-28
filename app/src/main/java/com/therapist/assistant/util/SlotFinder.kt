package com.therapist.assistant.util

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import java.util.Calendar

/**
 * پیشنهاد زمان خالی برای نوبت بعدی بر اساس جلسات موجود.
 */
object SlotFinder {

    data class FreeSlot(
        val startMillis: Long,
        val labelFa: String
    )

    /**
     * @param appointments همه نوبت‌های غیرلغو
     * @param dayStart ساعت شروع کاری (مثلاً 9)
     * @param dayEnd ساعت پایان کاری (مثلاً 20)
     * @param slotMinutes مدت هر اسلات (پیش‌فرض ۵۰ دقیقه)
     * @param daysAhead چند روز جلو را جستجو کند
     */
    fun suggest(
        appointments: List<Appointment>,
        fromMillis: Long = System.currentTimeMillis(),
        dayStartHour: Int = 9,
        dayEndHour: Int = 20,
        slotMinutes: Int = 50,
        gapMinutes: Int = 10,
        daysAhead: Int = 14,
        maxResults: Int = 8
    ): List<FreeSlot> {
        val busy = appointments
            .filter { it.status != AppointmentStatus.CANCELLED }
            .map { it.dateTimeMillis to (it.dateTimeMillis + slotMinutes * 60_000L) }

        val results = mutableListOf<FreeSlot>()
        val cal = Calendar.getInstance().apply {
            timeInMillis = fromMillis
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // از ساعت بعد شروع کن اگر امروز است
            if (get(Calendar.HOUR_OF_DAY) >= dayStartHour) {
                add(Calendar.MINUTE, ((get(Calendar.MINUTE) / 10) + 1) * 10 - get(Calendar.MINUTE))
            }
        }

        var dayOffset = 0
        while (dayOffset <= daysAhead && results.size < maxResults) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = fromMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, dayStartHour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // جمعه را رد نکن — کاربر ممکن است کار کند؛ فقط گذشته را رد کن
            var cursor = dayCal.timeInMillis
            val endOfDay = Calendar.getInstance().apply {
                timeInMillis = dayCal.timeInMillis
                set(Calendar.HOUR_OF_DAY, dayEndHour)
                set(Calendar.MINUTE, 0)
            }.timeInMillis

            while (cursor + slotMinutes * 60_000L <= endOfDay && results.size < maxResults) {
                if (cursor >= fromMillis) {
                    val slotEnd = cursor + slotMinutes * 60_000L
                    val overlaps = busy.any { (bStart, bEnd) ->
                        cursor < bEnd + gapMinutes * 60_000L && slotEnd + gapMinutes * 60_000L > bStart
                    }
                    if (!overlaps) {
                        results += FreeSlot(
                            startMillis = cursor,
                            labelFa = JalaliDate.formatDateTime(cursor)
                        )
                    }
                }
                cursor += (slotMinutes + gapMinutes) * 60_000L
            }
            dayOffset++
        }
        return results
    }
}
