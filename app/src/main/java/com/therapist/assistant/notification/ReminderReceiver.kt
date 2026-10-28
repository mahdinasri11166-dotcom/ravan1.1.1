package com.therapist.assistant.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.therapist.assistant.data.AppDatabase
import com.therapist.assistant.data.AppointmentStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra("appointmentId", -1L)
        when (intent.action) {
            NotificationHelper.ACTION_CONFIRM -> {
                if (id > 0) CoroutineScope(Dispatchers.IO).launch {
                    val dao = AppDatabase.getDatabase(context).appointmentDao()
                    dao.getAppointmentById(id)?.let {
                        dao.update(it.copy(status = AppointmentStatus.COMPLETED))
                    }
                }
            }
            NotificationHelper.ACTION_CANCEL -> {
                if (id > 0) CoroutineScope(Dispatchers.IO).launch {
                    val dao = AppDatabase.getDatabase(context).appointmentDao()
                    dao.getAppointmentById(id)?.let {
                        dao.update(it.copy(status = AppointmentStatus.CANCELLED, cancelReason = "لغو از اعلان"))
                        NotificationHelper.cancelReminders(context, id)
                    }
                }
            }
            else -> {
                val name = intent.getStringExtra("clientName") ?: return
                val dt = intent.getLongExtra("dateTimeMillis", 0L)
                val isDay = intent.getBooleanExtra("isDayBefore", true)
                if (id <= 0) return
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val appt = db.appointmentDao().getAppointmentById(id)
                        val phone = appt?.let { db.clientDao().getClientById(it.clientId)?.phone } ?: ""
                        NotificationHelper.showReminder(context, id, name, phone, dt, isDay)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }
}
