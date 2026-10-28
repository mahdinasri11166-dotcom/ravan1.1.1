package com.therapist.assistant.util

object ClinicalWorksheets {
    fun thoughtRecord(
        situation: String,
        emotion: String,
        thought: String,
        evidenceFor: String,
        evidenceAgainst: String,
        balanced: String,
        emotionAfter: String
    ): String = buildString {
        appendLine("── فرم فکر CBT ──")
        appendLine("موقعیت: $situation")
        appendLine("هیجان: $emotion")
        appendLine("فکر خودکار: $thought")
        appendLine("شواهد له: $evidenceFor")
        appendLine("شواهد علیه: $evidenceAgainst")
        appendLine("پاسخ متعادل: $balanced")
        appendLine("هیجان بعد: $emotionAfter")
        appendLine("── پایان فرم فکر ──")
    }

    fun baActivity(
        day: String,
        activity: String,
        pleasure: String,
        mastery: String,
        done: Boolean
    ): String = buildString {
        appendLine("── فعالیت BA ──")
        appendLine("روز: $day | فعالیت: $activity")
        appendLine("لذت: $pleasure | تسلط: $mastery | انجام: ${if (done) "بله" else "خیر"}")
        appendLine("── پایان BA ──")
    }

    fun safetyPlan(
        warning: String,
        coping: String,
        contacts: String,
        emergency: String,
        environment: String
    ): String = buildString {
        appendLine("── برنامه ایمنی ──")
        appendLine("نشانه‌های هشدار: $warning")
        appendLine("مهارت‌های آرام‌سازی: $coping")
        appendLine("افراد قابل تماس: $contacts")
        appendLine("اورژانس / مرکز: $emergency")
        appendLine("ایمن‌سازی محیط: $environment")
        appendLine("── پایان برنامه ایمنی ──")
    }

    fun extractSafetyPlan(notes: String): String? {
        val start = notes.indexOf("── برنامه ایمنی ──")
        if (start < 0) return null
        val end = notes.indexOf("── پایان برنامه ایمنی ──", start)
        return if (end > start) notes.substring(start, end + "── پایان برنامه ایمنی ──".length) else notes.substring(start)
    }
}