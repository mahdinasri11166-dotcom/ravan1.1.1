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

class DebtsWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_debts)
            val summary = runBlocking {
                try {
                    val list = AppDatabase.getDatabase(context).appointmentDao().getAllOnce()
                        .filter { !it.isPaid && it.status != AppointmentStatus.CANCELLED }
                    val total = list.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
                    val count = list.size
                    count to total
                } catch (_: Exception) {
                    0 to 0.0
                }
            }
            val (count, total) = summary
            if (count > 0) {
                views.setTextViewText(R.id.widget_debts_title, "${JalaliDate.toFa(count.toString())} بدهی باز")
                views.setTextViewText(
                    R.id.widget_debts_subtitle,
                    "${JalaliDate.toFa(total.toLong().toString())} تومان"
                )
            } else {
                views.setTextViewText(R.id.widget_debts_title, "بدهی‌ها")
                views.setTextViewText(R.id.widget_debts_subtitle, "بدهی باز ثبت نشده")
            }
            val pi = PendingIntent.getActivity(
                context, 1, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_debts_root, pi)
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
