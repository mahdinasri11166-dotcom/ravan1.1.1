package com.therapist.assistant.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.therapist.assistant.MainActivity
import com.therapist.assistant.R
import com.therapist.assistant.data.Appointment
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {
    const val CHANNEL_ID = "appointment_reminders"
    const val ACTION_CONFIRM = "com.therapist.assistant.CONFIRM"
    const val ACTION_CANCEL = "com.therapist.assistant.CANCEL"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "یادآوری نوبت‌ها", NotificationManager.IMPORTANCE_HIGH)
            ch.description = "یادآوری نوبت‌های پیش‌رو"
            ch.enableVibration(true)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    fun showReminder(
        context: Context,
        appointmentId: Long,
        clientName: String,
        phone: String,
        dateTimeMillis: Long,
        isDayBefore: Boolean
    ) {
        val tf = SimpleDateFormat("HH:mm", Locale("fa"))
        val df = SimpleDateFormat("yyyy/MM/dd", Locale("fa"))
        val title = if (isDayBefore) "یادآوری نوبت فردا" else "یادآوری نوبت امروز"
        val body = "$clientName — ${tf.format(Date(dateTimeMillis))} (${df.format(Date(dateTimeMillis))})"

        val openIntent = PendingIntent.getActivity(
            context, appointmentId.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val confirmPi = actionPi(context, ACTION_CONFIRM, appointmentId, 100)
        val cancelPi = actionPi(context, ACTION_CANCEL, appointmentId, 200)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .addAction(0, "تأیید", confirmPi)
            .addAction(0, "لغو", cancelPi)

        if (phone.isNotBlank()) {
            val callPi = PendingIntent.getActivity(
                context, (appointmentId + 300).toInt(),
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "تماس", callPi)
        }

        context.getSystemService(NotificationManager::class.java)
            .notify((appointmentId * 10 + if (isDayBefore) 1 else 2).toInt(), builder.build())
    }

    private fun actionPi(context: Context, action: String, apptId: Long, reqBase: Int): PendingIntent {
        val i = Intent(context, ReminderReceiver::class.java).apply {
            this.action = action
            putExtra("appointmentId", apptId)
        }
        return PendingIntent.getBroadcast(
            context, (apptId + reqBase).toInt(), i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleReminders(context: Context, appointment: Appointment, dayBefore: Boolean, hourBefore: Boolean, morning: Boolean = false) {
        if (appointment.status != com.therapist.assistant.data.AppointmentStatus.SCHEDULED) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (dayBefore) {
            val t = appointment.dateTimeMillis - 24 * 60 * 60 * 1000L
            if (t > System.currentTimeMillis()) scheduleExact(context, am, appointment, t, true, 1)
        }
        if (hourBefore) {
            val t = appointment.dateTimeMillis - 60 * 60 * 1000L
            if (t > System.currentTimeMillis()) scheduleExact(context, am, appointment, t, false, 2)
        }
        if (morning) {
            val cal = java.util.Calendar.getInstance().apply {
                timeInMillis = appointment.dateTimeMillis
                set(java.util.Calendar.HOUR_OF_DAY, 8)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
            }
            if (cal.timeInMillis < appointment.dateTimeMillis && cal.timeInMillis > System.currentTimeMillis()) {
                scheduleExact(context, am, appointment, cal.timeInMillis, false, 3)
            }
        }
    }

    /**
     * یادآوری هوشمند: اگر highRisk باشد، ۴۸ ساعت قبل هم یادآوری می‌شود.
     */
    fun scheduleSmartReminders(
        context: Context,
        appointment: Appointment,
        dayBefore: Boolean,
        hourBefore: Boolean,
        morning: Boolean = false,
        highRiskClient: Boolean = false
    ) {
        scheduleReminders(context, appointment, dayBefore, hourBefore, morning)
        if (highRiskClient) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val t48 = appointment.dateTimeMillis - 48L * 60 * 60 * 1000
            if (t48 > System.currentTimeMillis()) {
                scheduleExact(context, am, appointment, t48, true, 4)
            }
            // ۲ ساعت قبل هم برای ریسک بالا
            val t2h = appointment.dateTimeMillis - 2L * 60 * 60 * 1000
            if (t2h > System.currentTimeMillis()) {
                scheduleExact(context, am, appointment, t2h, false, 5)
            }
        }
    }

    private fun scheduleExact(ctx: Context, am: AlarmManager, a: Appointment, trigger: Long, dayBefore: Boolean, reqSuffix: Int) {
        val intent = Intent(ctx, ReminderReceiver::class.java).apply {
            putExtra("appointmentId", a.id)
            putExtra("clientName", a.clientName)
            putExtra("dateTimeMillis", a.dateTimeMillis)
            putExtra("isDayBefore", dayBefore)
        }
        val rc = (a.id * 10 + reqSuffix).toInt()
        val pi = PendingIntent.getBroadcast(ctx, rc, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
            else am.setExact(AlarmManager.RTC_WAKEUP, trigger, pi)
        } catch (_: SecurityException) {}
    }

    fun cancelReminders(context: Context, appointmentId: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(1, 2, 3, 4, 5).forEach { s ->
            val pi = PendingIntent.getBroadcast(
                context, (appointmentId * 10 + s).toInt(),
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }
    }


    private const val ONGOING_ID = 9001

    fun showOngoingNext(context: Context, clientName: String?, dateTimeMillis: Long?) {
        createChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (clientName == null || dateTimeMillis == null) {
            nm.cancel(ONGOING_ID)
            return
        }
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, com.therapist.assistant.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = "${clientName} — ${com.therapist.assistant.util.JalaliDate.formatDateTime(dateTimeMillis)}"
        val n = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("نوبت بعدی")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(open)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .build()
        nm.notify(ONGOING_ID, n)
    }

    fun cancelOngoing(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(ONGOING_ID)
    }
}
