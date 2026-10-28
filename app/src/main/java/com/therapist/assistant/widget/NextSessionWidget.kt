package com.therapist.assistant.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.therapist.assistant.MainActivity
import com.therapist.assistant.R
import com.therapist.assistant.data.AppDatabase
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.util.JalaliDate
import kotlinx.coroutines.runBlocking

class NextSessionWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_next_session)
            val next = runBlocking {
                try {
                    val now = System.currentTimeMillis()
                    AppDatabase.getDatabase(context).appointmentDao().getAllOnce()
                        .filter { it.status == AppointmentStatus.SCHEDULED && it.dateTimeMillis >= now }
                        .minByOrNull { it.dateTimeMillis }
                } catch (_: Exception) { null }
            }
            if (next != null) {
                views.setTextViewText(R.id.widget_title, next.clientName)
                views.setTextViewText(R.id.widget_subtitle, JalaliDate.formatDateTime(next.dateTimeMillis))
            } else {
                views.setTextViewText(R.id.widget_title, "نوبت بعدی")
                views.setTextViewText(R.id.widget_subtitle, "جلسه‌ای زمان‌بندی نشده")
            }
            val pi = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
