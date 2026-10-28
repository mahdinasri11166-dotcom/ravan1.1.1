package com.therapist.assistant.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.therapist.assistant.data.AppDatabase
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * پس از ریبوت، یادآوری نوبت‌های پیش‌رو را دوباره زمان‌بندی می‌کند.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsRepository(context)
                if (!settings.remindersEnabled.first()) return@launch
                val dayBefore = settings.remindDayBefore.first()
                val hourBefore = settings.remindHourBefore.first()
                val db = AppDatabase.getDatabase(context)
                val now = System.currentTimeMillis()
                // خواندن نوبت‌های آینده (با query ساده)
                val upcoming = db.appointmentDao().getUpcomingAppointments(now)
                // چون Flow است، یک بار first می‌گیریم
                val list = upcoming.first()
                list.filter { it.status == AppointmentStatus.SCHEDULED }.forEach { appt ->
                    NotificationHelper.scheduleReminders(context, appt, dayBefore, hourBefore)
                }
            } catch (_: Exception) {
                // نادیده گرفتن خطا در بوت
            } finally {
                pendingResult.finish()
            }
        }
    }
}
