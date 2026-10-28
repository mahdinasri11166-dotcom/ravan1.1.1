package com.therapist.assistant.util

import java.util.Calendar
import java.util.Locale

/**
 * تبدیل تاریخ میلادی ↔ شمسی و قالب‌بندی فارسی
 */
object JalaliDate {

    data class Date(val year: Int, val month: Int, val day: Int) {
        fun format(): String = "%04d/%02d/%02d".format(year, month, day)
        fun formatFa(): String = "${toFa(year)}/${toFa(month.toString().padStart(2, '0'))}/${toFa(day.toString().padStart(2, '0'))}"
    }

    private val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    private val weekDays = arrayOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")

    fun fromMillis(millis: Long): Date {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun formatDate(millis: Long): String = fromMillis(millis).formatFa()

    fun formatDateLong(millis: Long): String {
        val j = fromMillis(millis)
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val wd = weekDays[cal.get(Calendar.DAY_OF_WEEK) - 1]
        return "$wd ${toFa(j.day)} ${monthNames[j.month - 1]} ${toFa(j.year)}"
    }

    fun formatTime(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        return "${toFa(h.toString().padStart(2, '0'))}:${toFa(m.toString().padStart(2, '0'))}"
    }

    fun formatDateTime(millis: Long): String = "${formatDate(millis)}  ${formatTime(millis)}"

    fun formatMonthYear(millis: Long): String {
        val j = fromMillis(millis)
        return "${monthNames[j.month - 1]} ${toFa(j.year)}"
    }

    fun toFa(n: Int): String = toFa(n.toString())
    fun toFa(s: String): String = buildString {
        for (c in s) {
            append(if (c in '0'..'9') '۰' + (c - '0') else c)
        }
    }

    /** الگوریتم استاندارد تبدیل میلادی به شمسی */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Date {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var gy2 = gy
        val gm2 = gm
        val gd2 = gd
        var jy: Int
        if (gy2 > 1600) {
            jy = 979
            gy2 -= 1600
        } else {
            jy = 0
            gy2 -= 621
        }
        val gy2p = if (gm2 > 2) gy2 + 1 else gy2
        var days = 365 * gy2 + (gy2p + 3) / 4 - (gy2p + 99) / 100 + (gy2p + 399) / 400 - 80 + gd2 + gDaysInMonth[gm2 - 1]
        jy += 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return Date(jy, jm, jd)
    }

    fun jalaliToGregorianMillis(jy: Int, jm: Int, jd: Int, hour: Int = 0, minute: Int = 0): Long {
        val g = jalaliToGregorian(jy, jm, jd)
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, g.first)
            set(Calendar.MONTH, g.second - 1)
            set(Calendar.DAY_OF_MONTH, g.third)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        var jy2 = jy
        var gy: Int
        if (jy2 > 979) {
            gy = 1600
            jy2 -= 979
        } else {
            gy = 621
        }
        val days = 365 * jy2 + jy2 / 33 * 8 + (jy2 % 33 + 3) / 4 + 78 + jd +
            if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186
        gy += 400 * (days / 146097)
        var d = days % 146097
        if (d > 36524) {
            gy += 100 * ((d - 1) / 36524)
            d = (d - 1) % 36524
            if (d >= 365) d++
        }
        gy += 4 * (d / 1461)
        d %= 1461
        if (d > 365) {
            gy += (d - 1) / 365
            d = (d - 1) % 365
        }
        var gd = d + 1
        val salA = if ((gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0) 1 else 0
        val months = intArrayOf(0, 31, 28 + salA, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && gd > months[gm]) {
            gd -= months[gm]
            gm++
        }
        return Triple(gy, gm, gd)
    }
}
