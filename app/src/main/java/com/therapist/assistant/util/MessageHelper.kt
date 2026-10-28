package com.therapist.assistant.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

object MessageHelper {
    fun buildReminderText(
        template: String,
        name: String,
        dateTimeMillis: Long,
        clinicName: String = ""
    ): String {
        val base = template.ifBlank {
            "یادآوری نوبت\n{name} عزیز، نوبت شما در {clinic} روز {date} ساعت {time} است."
        }
        return base
            .replace("{name}", name)
            .replace("{date}", JalaliDate.formatDate(dateTimeMillis))
            .replace("{time}", JalaliDate.formatTime(dateTimeMillis))
            .replace("{clinic}", clinicName.ifBlank { "مرکز" })
    }

    private fun safeStart(context: Context, intent: Intent, chooserTitle: String? = null) {
        try {
            val launch = if (chooserTitle != null) {
                Intent.createChooser(intent, chooserTitle)
            } else intent
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
        } catch (t: Throwable) {
            Log.e("MessageHelper", "start failed", t)
            Toast.makeText(context, "برنامه مناسب یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }

    fun buildDebtText(template: String, name: String, amount: String, clinicName: String): String {
        val base = template.ifBlank { "سلام {name}، یادآوری بدهی در {clinic}. مبلغ: {amount}" }
        return base.replace("{name}", name).replace("{amount}", amount).replace("{clinic}", clinicName.ifBlank { "مرکز" })
    }

    fun buildBirthdayText(template: String, name: String, clinicName: String): String {
        val base = template.ifBlank { "{name} عزیز، تولدتان مبارک از طرف {clinic}" }
        return base.replace("{name}", name).replace("{clinic}", clinicName.ifBlank { "مرکز" })
    }

    fun openSms(context: Context, phone: String, text: String) {
        val p = phone.filter { it.isDigit() }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$p")
            putExtra("sms_body", text)
        }
        safeStart(context, intent, "ارسال پیامک")
    }

    fun openWhatsApp(context: Context, phone: String, text: String) {
        var p = phone.filter { it.isDigit() }
        if (p.startsWith("0")) p = "98" + p.drop(1)
        val url = "https://wa.me/$p?text=${Uri.encode(text)}"
        safeStart(context, Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun openEitaa(context: Context, phone: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        safeStart(context, intent, "ارسال با ایتا / سایر")
    }
}
