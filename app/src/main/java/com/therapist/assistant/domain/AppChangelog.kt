package com.therapist.assistant.domain

object AppChangelog {
    data class Entry(val version: String, val items: List<String>)

    val entries: List<Entry> = listOf(
        Entry(
            "1.1.1",
            listOf(
                "بازیابی با Share فایل به اپ + هشدار تعداد مراجع/نوبت",
                "Excel مالی ماهانه، فیلتر درمانگر در تقویم، تم تیره سیستم",
                "اعلان ماندگار نوبت بعدی، چند قالب پیام، راهنمای Drive",
                "چک‌لیست جلسه، پیگیری غیبت، سقف نوبت روزانه",

                "SAF با رمز اختیاری برای پوشه Drive",
                "بازیابی از لیست فایل‌های پوشه پشتیبان",
                "پشتیبان خودکار روزانه در پوشه SAF",
                "نمایش مسیر و حجم آخرین بکاپ",
                "PDF مالی ماهانه",
                "ویجت بدهی‌ها",
                "آمار سالانه در روند درمان",
                "جستجوی سراسری روی SOAP/یادداشت",
                "نمای عریض تبلت در تقویم و جلسات"
            )
        ),
        Entry(
            "5.14.0",
            listOf(
                "تم صفحه امروز، فاکتور PDF، جابجایی نوبت، یادآوری هوشمند",
                "خروجی کارگاه آماری، کارت ویزیت تصویری، قفل خودکار"
            )
        ),
        Entry("5.13.0", listOf("پروتکل جلسه، روند درمان، کارگاه آموزشی"))
    )

    fun forVersion(version: String): Entry? =
        entries.find { version.startsWith(it.version) } ?: entries.firstOrNull()
}
