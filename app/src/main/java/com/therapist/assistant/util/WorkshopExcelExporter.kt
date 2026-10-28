package com.therapist.assistant.util

import com.therapist.assistant.data.Workshop
import com.therapist.assistant.data.WorkshopParticipant
import java.io.File

/**
 * خروجی اکسل رسمی کارگاه — منطبق با فایل‌های مرجع مراکز
 */
object WorkshopExcelExporter {

    fun buildWorkbookFile(file: File, workshop: Workshop, participants: List<WorkshopParticipant>) {
        XlsxWriter.write(
            file,
            listOf(
                sheetSummary(workshop, participants),
                sheetParticipants(workshop, participants),
                sheetQuantitative(workshop, participants),
                sheetOtherActions(workshop),
                sheetSurvey16(workshop),
                sheetSurvey4(workshop)
            )
        )
    }

    /** برگه خلاصه مدیریتی — آمار و بهبود نمرات */
    private fun sheetSummary(w: Workshop, parts: List<WorkshopParticipant>): XlsxWriter.Sheet {
        val rows = mutableListOf<List<String>>()
        rows.add(listOf("خلاصه گزارش کارگاه — درمانگرنو"))
        rows.add(listOf("عنوان", w.title.ifBlank { "کارگاه آموزشی" }))
        rows.add(listOf("استان", w.province.ifBlank { "خوزستان" }, "شهرستان", w.city.ifBlank { "اهواز" }))
        rows.add(listOf("مرکز مجری", w.centerName.ifBlank { "گل سرخ" }, "مکان", w.location.ifBlank { "—" }))
        rows.add(listOf("آموزشگر", w.trainerName.ifBlank { "—" }, "مدت (ساعت)", w.durationHours.toString()))
        rows.add(listOf("تاریخ برگزاری", JalaliDate.formatDateTime(w.dateMillis)))
        rows.add(listOf("سرفصل‌ها", w.topics.ifBlank { "—" }))
        rows.add(listOf())
        rows.add(listOf("شاخص", "مقدار"))
        val headerIdx = rows.size - 1
        rows.add(listOf("تعداد شرکت‌کنندگان", parts.size.toString()))

        val preScores = parts.mapNotNull { it.preTestScore.trim().toDoubleOrNull() }
        val postScores = parts.mapNotNull { it.postTestScore.trim().toDoubleOrNull() }
        val paired = parts.mapNotNull { p ->
            val a = p.preTestScore.trim().toDoubleOrNull()
            val b = p.postTestScore.trim().toDoubleOrNull()
            if (a != null && b != null) a to b else null
        }
        if (preScores.isNotEmpty()) {
            rows.add(listOf("میانگین پیش‌آزمون", "%.2f".format(preScores.average())))
        }
        if (postScores.isNotEmpty()) {
            rows.add(listOf("میانگین پس‌آزمون", "%.2f".format(postScores.average())))
        }
        if (paired.isNotEmpty()) {
            val avgDelta = paired.map { it.second - it.first }.average()
            val improved = paired.count { it.second > it.first }
            rows.add(listOf("میانگین تغییر نمره (پس − پیش)", "%.2f".format(avgDelta)))
            rows.add(listOf("تعداد بهبودیافته", "$improved از ${paired.size}"))
            val pct = if (paired.isNotEmpty()) improved * 100.0 / paired.size else 0.0
            rows.add(listOf("درصد بهبود", "%.0f%%".format(pct)))
        } else {
            rows.add(listOf("وضعیت نمرات", "نمرات پیش/پس برای محاسبه بهبود کامل نیست"))
        }
        rows.add(listOf())
        rows.add(listOf("تولید شده توسط", "درمانگرنو", "تاریخ خروجی", JalaliDate.formatDateTime(System.currentTimeMillis())))
        return XlsxWriter.Sheet(
            name = "خلاصه",
            rows = rows,
            headerRowIndices = setOf(0, headerIdx),
            colWidths = listOf(36.0, 28.0, 16.0, 22.0)
        )
    }

    private fun sheetParticipants(w: Workshop, parts: List<WorkshopParticipant>): XlsxWriter.Sheet {
        val courseKind = courseLabel(w)
        val rows = mutableListOf<List<String>>()

        rows.add(listOf("جدول مشخصات شرکت‌کنندگان آموزش $courseKind"))
        rows.add(
            listOf(
                "استان برگزارکننده",
                w.province.ifBlank { "خوزستان" },
                "شهرستان",
                w.city.ifBlank { "اهواز" },
                "مرکز مجری کارگاه",
                w.centerName.ifBlank { "گل سرخ" },
                "مکان برگزاری",
                w.location.ifBlank { w.city.ifBlank { "اهواز" } }
            )
        )
        rows.add(
            listOf(
                "عنوان دوره",
                w.title.ifBlank { courseKind },
                "مدت زمان کارگاه",
                formatDuration(w.durationHours),
                "سرفصل‌های آموزشی",
                w.topics.ifBlank { w.title },
                "نام آموزشگر",
                w.trainerName
            )
        )
        rows.add(
            listOf(
                "تاریخ و ساعت برگزاری",
                JalaliDate.formatDateTime(w.dateMillis),
                "تعداد شرکت‌کنندگان",
                parts.size.toString(),
                "", "", "", ""
            )
        )
        rows.add(listOf())

        rows.add(
            listOf(
                "ردیف",
                "نام و نام خانوادگی",
                "تاریخ تولد",
                "مدرک تحصیلی",
                "کد ملی",
                "نمره پیش‌آزمون",
                "نمره پس‌آزمون",
                "تغییر نمره",
                "شماره تماس"
            )
        )
        val headerRow = rows.size

        parts.forEachIndexed { index, p ->
            val pre = p.preTestScore.trim().toDoubleOrNull()
            val post = p.postTestScore.trim().toDoubleOrNull()
            val delta = if (pre != null && post != null) {
                val d = post - pre
                if (d > 0) "+${"%.1f".format(d)}" else "%.1f".format(d)
            } else "—"
            rows.add(
                listOf(
                    (index + 1).toString(),
                    p.fullName,
                    p.birthDateText,
                    p.education,
                    p.nationalId,
                    p.preTestScore,
                    p.postTestScore,
                    delta,
                    p.phone
                )
            )
        }
        val minRows = 10
        if (parts.size < minRows) {
            repeat(minRows - parts.size) {
                rows.add(listOf("", "", "", "", "", "", ""))
            }
        }

        return XlsxWriter.Sheet(
            name = "مشخصات شرکت‌کنندگان",
            rows = rows,
            headerRowIndices = setOf(headerRow),
            colWidths = listOf(8.0, 22.0, 14.0, 14.0, 14.0, 12.0, 12.0, 12.0, 14.0)
        )
    }

    private fun sheetQuantitative(w: Workshop, parts: List<WorkshopParticipant>): XlsxWriter.Sheet {
        val isFamily = isFamilyCourse(w)
        val rows = mutableListOf<List<String>>()

        if (isFamily) {
            rows.add(listOf("جدول گزارش کمی دوره‌های آموزش زندگی خانواده — شناسنامه فراگیران (مخاطب اصلی) دوره‌های آموزشی"))
        } else {
            rows.add(listOf("جدول گزارش کمی دوره‌های آموزشی پیش از ازدواج — شناسنامه فراگیران (مخاطب اصلی) دوره‌های آموزشی"))
        }

        rows.add(
            listOf(
                "نام استان",
                w.province.ifBlank { "خوزستان" },
                "شهرستان",
                w.city.ifBlank { "اهواز" },
                "سال و ماه‌های اجرا",
                JalaliDate.formatMonthYear(w.dateMillis),
                "عنوان دوره",
                w.title
            )
        )
        rows.add(listOf())

        val edu = countEducation(parts)
        rows.add(listOf("وضعیت تحصیلات فراگیران"))
        rows.add(listOf("زیر دیپلم", "دیپلم", "لیسانس", "فوق لیسانس بالاتر", "جمع کل"))
        val eduHeader = rows.size
        rows.add(
            listOf(
                edu.underDiploma.toString(),
                edu.diploma.toString(),
                edu.bachelor.toString(),
                edu.masterPlus.toString(),
                parts.size.toString()
            )
        )
        rows.add(listOf())

        val ages = parts.mapNotNull { approxAge(it.birthDateText) }
        if (isFamily) {
            rows.add(listOf("وضعیت سن فراگیران"))
            rows.add(
                listOf(
                    "۰–۵ سال (زن)", "۰–۵ سال (مرد)",
                    "۵ سال به بالا (زن)", "۵ سال به بالا (مرد)",
                    "جمع زنان", "جمع مردان", "جمع کل"
                )
            )
            val ageHeader = rows.size
            val over5 = ages.count { it > 5 }
            val under5 = ages.count { it in 0..5 }
            val unk = parts.size - under5 - over5
            rows.add(
                listOf(
                    "0", under5.toString(),
                    "0", over5.toString(),
                    "0", (under5 + over5).toString(),
                    parts.size.toString()
                )
            )
            if (unk > 0) {
                rows.add(listOf("تعداد بدون تاریخ تولد معتبر", unk.toString()))
            }
            rows.add(listOf())
            rows.add(listOf("تعداد کارگاه برگزار شده"))
            rows.add(listOf("مرکز استان", "شهرستان", "جمع کل"))
            val wsHeader = rows.size
            rows.add(listOf(w.province.ifBlank { "خوزستان" }, w.city.ifBlank { "اهواز" }, parts.size.toString()))

            return XlsxWriter.Sheet(
                name = "گزارش فراگیران",
                rows = rows,
                headerRowIndices = setOf(eduHeader, ageHeader, wsHeader),
                colWidths = listOf(16.0, 16.0, 18.0, 18.0, 12.0, 12.0, 12.0, 12.0)
            )
        } else {
            rows.add(listOf("وضعیت سن فراگیران"))
            rows.add(
                listOf(
                    "۱۵–۱۹ زن", "۱۵–۱۹ مرد",
                    "۲۰–۲۴ زن", "۲۰–۲۴ مرد",
                    "۲۵–۲۹ زن", "۲۵–۲۹ مرد",
                    "۳۰ به بالا زن", "۳۰ به بالا مرد",
                    "جمع زنان", "جمع مردان"
                )
            )
            val ageHeader = rows.size
            val c15 = ages.count { it in 15..19 }
            val c20 = ages.count { it in 20..24 }
            val c25 = ages.count { it in 25..29 }
            val c30 = ages.count { it >= 30 }
            rows.add(
                listOf(
                    "0", c15.toString(),
                    "0", c20.toString(),
                    "0", c25.toString(),
                    "0", c30.toString(),
                    "0", (c15 + c20 + c25 + c30).toString()
                )
            )
            rows.add(listOf())
            rows.add(listOf("تعداد کارگاه برگزار شده"))
            rows.add(listOf("مرکز استان", "شهرستان", "جمع کل"))
            val wsHeader = rows.size
            rows.add(listOf(w.province.ifBlank { "خوزستان" }, w.city.ifBlank { "اهواز" }, parts.size.toString()))
            rows.add(listOf())
            rows.add(listOf("فهرست شرکت‌کنندگان (از شیت مشخصات)"))
            rows.add(listOf("نام و نام خانوادگی", "تاریخ تولد", "مدرک تحصیلی", "کد ملی", "شماره تماس"))
            val listHeader = rows.size
            parts.forEach { p ->
                rows.add(listOf(p.fullName, p.birthDateText, p.education, p.nationalId, p.phone))
            }

            return XlsxWriter.Sheet(
                name = "گزارش فراگیران",
                rows = rows,
                headerRowIndices = setOf(eduHeader, ageHeader, wsHeader, listHeader),
                colWidths = listOf(12.0, 12.0, 12.0, 12.0, 12.0, 12.0, 14.0, 14.0, 12.0, 12.0)
            )
        }
    }

    private fun sheetOtherActions(w: Workshop): XlsxWriter.Sheet {
        val kind = courseLabel(w)
        val rows = mutableListOf<List<String>>()
        rows.add(listOf("سایر اقدامات انجام‌شده در خصوص دوره آموزش $kind"))
        rows.add(
            listOf(
                "استان", w.province,
                "مرکز", w.centerName,
                "عنوان دوره", w.title,
                "تاریخ", JalaliDate.formatDate(w.dateMillis)
            )
        )
        rows.add(listOf())
        rows.add(listOf("عنوان اقدام", "تعداد", "موضوع و محتوا", "شرح اقدام"))
        val h = rows.size
        repeat(15) { rows.add(listOf("", "", "", "")) }
        return XlsxWriter.Sheet(
            name = "سایر اقدامات آموزشی",
            rows = rows,
            headerRowIndices = setOf(h),
            colWidths = listOf(24.0, 10.0, 28.0, 40.0)
        )
    }

    private fun sheetSurvey16(w: Workshop): XlsxWriter.Sheet {
        val rows = mutableListOf<List<String>>()
        rows.add(listOf("فرم نظرسنجی کارگاه مخاطب اصلی آموزش ${courseLabel(w)} (ویژه دوره‌های ۱۶ ساعته)"))
        rows.add(
            listOf(
                "نام استان", w.province.ifBlank { "خوزستان" },
                "عنوان دوره", w.title,
                "تاریخ برگزاری", JalaliDate.formatDate(w.dateMillis)
            )
        )
        rows.add(listOf("نام مدرس", w.trainerName, "تحصیلات", "", "جنسیت", ""))
        rows.add(listOf())
        rows.add(listOf("محورها", "سؤالات", "کاملاً مخالفم", "مخالفم", "نظری ندارم", "موافقم"))
        val headerRow = rows.size
        val items = listOf(
            "محتوای کارگاه" to "مطالب ارائه‌شده مفید و کاربردی بوده است.",
            "محتوای کارگاه" to "مطالب ارائه‌شده در کارگاه باعث تعمیق یا تصحیح نسبت به موضوع تدریس شد.",
            "محتوای کارگاه" to "مطالب ارائه‌شده در کارگاه باعث ترغیب به مطالعه و تحقیق بیشتر در من شد.",
            "محتوای کارگاه" to "انسجام و پیوستگی در ارائه مطالب مطرح‌شده در دوره به چشم می‌خورد.",
            "محتوای کارگاه" to "مطالب ارائه‌شده باعث افزایش اطلاعاتم در رابطه با موضوع تدریس شد.",
            "طراحی کارگاه" to "اهداف کارگاه برای من روشن و واضح بود.",
            "طراحی کارگاه" to "فعالیت‌های انجام‌شده در طی کارگاه باعث تحریک یادگیری من شد.",
            "طراحی کارگاه" to "فعالیت‌های انجام‌شده در طی کارگاه مهارت لازم را در من ایجاد کرد.",
            "طراحی کارگاه" to "روش‌ها و شیوه‌های آموزشی مورد استفاده در کارگاه مناسب بود.",
            "طراحی کارگاه" to "فرصت مناسبی جهت مشارکت در این کارگاه برایم فراهم شد.",
            "مدرس کارگاه" to "مدرس نسبت به انجام وظایف دغدغه و دلسوزی داشت.",
            "مدرس کارگاه" to "اصول اخلاقی و آداب اجتماعی در طول دوره توسط مدرس رعایت شد.",
            "مدرس کارگاه" to "برخورد مربی با عقاید و آراء شرکت‌کنندگان مناسب بود.",
            "مدرس کارگاه" to "مدرس در اداره کلاس موفق بود.",
            "مدرس کارگاه" to "مهارت مدرس در انتقال مطالب و شیوایی بیان مناسب بود.",
            "مدرس کارگاه" to "میزان مشارکت فراگیران در طول دوره مناسب بود.",
            "مدرس کارگاه" to "قدرت مربی در پاسخگویی به سؤالات و شبهات فراگیران مناسب بود.",
            "مدرس کارگاه" to "تسلط علمی مربی مناسب بود.",
            "مدرس کارگاه" to "میزان استقبال استاد از اظهارنظر و طرح سؤال از طرف شرکت‌کنندگان مناسب بود.",
            "نظرخواهی در خصوص نحوه ثبت‌نام در دوره آموزشی" to "ارزیابی شما از نحوه اطلاع‌رسانی و ثبت‌نام دوره‌ها رضایت‌بخش بود.",
            "مکان برگزاری کارگاه" to "مکان برگزاری کارگاه مناسب بود.",
            "نظرخواهی در خصوص برگزاری کارگاه" to "برنامه‌ریزی اجرایی از نظر ساعت و زمان برگزاری خوب بود.",
            "نظرخواهی در خصوص برگزاری کارگاه" to "نظم و انضباط مؤسسه در برگزاری به‌موقع کارگاه‌ها مناسب بود.",
            "نظرخواهی در خصوص برگزاری کارگاه" to "امکانات مؤسسه از نظر فضا، نور، تهویه و وسائل کمک‌آموزشی مورد تأیید بود.",
            "نظرخواهی در خصوص برگزاری کارگاه" to "نحوه پذیرایی بین جلسات و برخورد مسئولین سازمان مناسب بود.",
            "نتایج کارگاه" to "می‌توانم آنچه را در کارگاه آموختم به کار بگیرم.",
            "نتایج کارگاه" to "کارگاه روش خوبی برای یادگیری محتوا بود."
        )
        items.forEach { (axis, q) -> rows.add(listOf(axis, q, "", "", "", "")) }
        rows.add(listOf())
        rows.add(listOf("پیشنهاد شما برای برگزاری بهتر کارگاه‌های آموزشی چیست؟"))
        rows.add(listOf("", "", "", "", "", ""))
        rows.add(listOf("", "", "", "", "", ""))
        return XlsxWriter.Sheet(
            name = "فرم نظرسنجی ۱۶ ساعته",
            rows = rows,
            headerRowIndices = setOf(headerRow),
            colWidths = listOf(28.0, 55.0, 12.0, 10.0, 12.0, 10.0)
        )
    }

    private fun sheetSurvey4(w: Workshop): XlsxWriter.Sheet {
        val rows = mutableListOf<List<String>>()
        rows.add(listOf("فرم نظرسنجی کارگاه مخاطب اصلی آموزش ${courseLabel(w)} (ویژه دوره‌های ۴ ساعته)"))
        rows.add(
            listOf(
                "نام استان", w.province.ifBlank { "خوزستان" },
                "عنوان دوره", w.title,
                "تاریخ برگزاری", JalaliDate.formatDate(w.dateMillis)
            )
        )
        rows.add(listOf("نام مدرس", w.trainerName, "تحصیلات", "", "جنسیت", ""))
        rows.add(listOf())
        rows.add(listOf("محورها", "سؤالات", "کاملاً مخالفم", "مخالفم", "نظری ندارم", "موافقم"))
        val headerRow = rows.size
        val items = listOf(
            "محتوای کارگاه" to "مطالب ارائه‌شده مفید و کاربردی بوده است.",
            "محتوای کارگاه" to "مطالب ارائه‌شده در کارگاه باعث تعمیق یا تصحیح نسبت به موضوع تدریس شد.",
            "محتوای کارگاه" to "مطالب ارائه‌شده در کارگاه باعث ترغیب به مطالعه و تحقیق بیشتر در من شد.",
            "محتوای کارگاه" to "انسجام و پیوستگی در ارائه مطالب مطرح‌شده در دوره به چشم می‌خورد.",
            "محتوای کارگاه" to "مطالب ارائه‌شده باعث افزایش اطلاعاتم در رابطه با موضوع تدریس شد.",
            "مدرس کارگاه" to "مدرس نسبت به انجام وظایف دغدغه و دلسوزی داشت.",
            "مدرس کارگاه" to "اصول اخلاقی و آداب اجتماعی در طول دوره توسط مدرس رعایت شد.",
            "مدرس کارگاه" to "برخورد مربی با عقاید و آراء شرکت‌کنندگان مناسب بود.",
            "مدرس کارگاه" to "مدرس در اداره کلاس موفق بود.",
            "مدرس کارگاه" to "مهارت مدرس در انتقال مطالب و شیوایی بیان مناسب بود.",
            "مدرس کارگاه" to "میزان مشارکت فراگیران در طول دوره مناسب بود.",
            "مدرس کارگاه" to "قدرت مربی در پاسخگویی به سؤالات و شبهات فراگیران مناسب بود.",
            "مدرس کارگاه" to "تسلط علمی مربی مناسب بود.",
            "مکان برگزاری کارگاه" to "مکان برگزاری کارگاه مناسب بود.",
            "نتیجه کارگاه" to "این کارگاه روش خوبی برای یادگیری محتوا بود."
        )
        items.forEach { (axis, q) -> rows.add(listOf(axis, q, "", "", "", "")) }
        rows.add(listOf())
        rows.add(listOf("پیشنهاد شما برای برگزاری بهتر کارگاه‌های آموزشی چیست؟"))
        rows.add(listOf("", "", "", "", "", ""))
        rows.add(listOf("", "", "", "", "", ""))
        return XlsxWriter.Sheet(
            name = "فرم نظرسنجی ۴ ساعته",
            rows = rows,
            headerRowIndices = setOf(headerRow),
            colWidths = listOf(22.0, 55.0, 12.0, 10.0, 12.0, 10.0)
        )
    }

    private fun courseLabel(w: Workshop): String = when {
        isFamilyCourse(w) -> "زندگی خانواده"
        w.title.contains("ازدواج") || w.title.contains("پیش از") -> "پیش از ازدواج"
        else -> w.title.ifBlank { "کارگاه آموزشی" }
    }

    private fun isFamilyCourse(w: Workshop): Boolean =
        w.title.contains("خانواده") || w.topics.contains("خانواده")

    private fun formatDuration(hours: Int): String =
        if (hours <= 0) "۴ ساعت" else "$hours ساعت"

    private data class EduCount(
        val underDiploma: Int,
        val diploma: Int,
        val bachelor: Int,
        val masterPlus: Int
    )

    private fun countEducation(parts: List<WorkshopParticipant>): EduCount {
        var under = 0
        var dip = 0
        var bs = 0
        var ms = 0
        parts.forEach { p ->
            val t = p.education.trim()
            when {
                t.contains("فوق") && (t.contains("لیسانس") || t.contains("ارشد")) ||
                    t.contains("دکت") || t.contains("ارشد") -> ms++
                t.contains("لیسانس") || t.contains("کارشناسی") -> bs++
                t.contains("کاردانی") || t.contains("فوق دیپلم") -> bs++
                t.contains("دیپلم") -> dip++
                else -> under++
            }
        }
        return EduCount(under, dip, bs, ms)
    }

    private fun approxAge(birth: String): Int? {
        val year = Regex("""(13\d{2}|14\d{2})""").find(birth)?.value?.toIntOrNull() ?: return null
        val nowY = try {
            JalaliDate.fromMillis(System.currentTimeMillis()).year
        } catch (_: Exception) {
            1404
        }
        return (nowY - year).takeIf { it in 5..100 }
    }
}
