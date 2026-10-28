package com.therapist.assistant.util

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.QuestionnaireResult
import java.io.File
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

object MonthlyReportExporter {

    fun export(
        file: File,
        clinicName: String,
        clinicPhone: String,
        clinicAddress: String,
        appointments: List<Appointment>,
        questionnaires: List<QuestionnaireResult>,
        year: Int,
        month: Int
    ) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis

        val monthAppts = appointments.filter { it.dateTimeMillis in start until end }
        val currency = NumberFormat.getNumberInstance(Locale("fa"))
        val income = monthAppts.filter { it.isPaid }.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
        val unpaid = monthAppts.filter {
            !it.isPaid && (it.fee - it.discount) > 0 && it.status != AppointmentStatus.CANCELLED
        }
        val unpaidSum = unpaid.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
        val done = monthAppts.count { it.status == AppointmentStatus.COMPLETED }
        val cancel = monthAppts.count { it.status == AppointmentStatus.CANCELLED }
        val cancelRate = if (monthAppts.isEmpty()) 0 else cancel * 100 / monthAppts.size
        val qCount = questionnaires.count { it.createdAt in start until end }

        val weekIncome = weekBuckets(monthAppts)
        val byTherapist = monthAppts.groupBy { it.therapistName.ifBlank { "بدون نام" } }.map { (name, list) ->
            val inc = list.filter { it.isPaid }.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
            listOf(name, list.size.toString(), currency.format(inc.toLong()))
        }

        val brand = PdfExporter.Brand(
            title = clinicName.ifBlank { "مرکز گل سرخ" },
            subtitle = "گزارش مدیریتی ماهانه",
            phone = clinicPhone,
            address = clinicAddress
        )

        val extra = mutableListOf<PdfExporter.NamedTable>()
        extra += PdfExporter.NamedTable(
            title = "۱. خلاصه شاخص‌ها",
            columns = listOf(
                PdfExporter.TableColumn("شاخص", 0.55f),
                PdfExporter.TableColumn("مقدار", 0.45f)
            ),
            rows = listOf(
                listOf("دوره", JalaliDate.formatMonthYear(start)),
                listOf("کل نوبت‌ها", monthAppts.size.toString()),
                listOf("انجام‌شده", done.toString()),
                listOf("لغو‌شده", cancel.toString()),
                listOf("نرخ لغو", "$cancelRate٪"),
                listOf("درآمد وصول‌شده (تومان)", currency.format(income.toLong())),
                listOf("بدهی دوره (تومان)", currency.format(unpaidSum.toLong())),
                listOf("تعداد آزمون ثبت‌شده", qCount.toString())
            )
        )
        extra += PdfExporter.NamedTable(
            title = "۲. درآمد هفتگی در ماه",
            columns = listOf(
                PdfExporter.TableColumn("هفته", 0.4f),
                PdfExporter.TableColumn("وصول (تومان)", 0.6f)
            ),
            rows = weekIncome.mapIndexed { i, v -> listOf("هفته ${i + 1}", currency.format(v.toLong())) }
        )
        if (byTherapist.isNotEmpty()) {
            extra += PdfExporter.NamedTable(
                title = "۳. به‌تفکیک درمانگر",
                columns = listOf(
                    PdfExporter.TableColumn("درمانگر", 0.4f),
                    PdfExporter.TableColumn("نوبت", 0.2f),
                    PdfExporter.TableColumn("وصول", 0.4f)
                ),
                rows = byTherapist
            )
        }
        if (unpaid.isNotEmpty()) {
            extra += PdfExporter.NamedTable(
                title = "۴. بدهکاران دوره",
                columns = listOf(
                    PdfExporter.TableColumn("مراجع", 0.45f),
                    PdfExporter.TableColumn("مبلغ", 0.55f)
                ),
                rows = unpaid.groupBy { it.clientName }.map { (n, list) ->
                    listOf(
                        n,
                        currency.format(list.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }.toLong())
                    )
                }
            )
        }

        PdfExporter.writeStyledPdf(
            file = file,
            brand = brand,
            introLines = listOf(
                "این گزارش مدیریتی است و جزئیات محرمانه جلسات را شامل نمی‌شود.",
                "تاریخ تهیه: ${JalaliDate.formatDateLong(System.currentTimeMillis())}"
            ),
            extraTables = extra,
            footer = "درمانگرنو — گزارش مدیریتی"
        )
    }

    private fun weekBuckets(appts: List<Appointment>): List<Double> {
        if (appts.isEmpty()) return listOf(0.0, 0.0, 0.0, 0.0)
        val minT = appts.minOf { it.dateTimeMillis }
        val cal = Calendar.getInstance().apply {
            timeInMillis = minT
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
        }
        val start = cal.timeInMillis
        val buckets = DoubleArray(5)
        for (a in appts.filter { it.isPaid }) {
            val week = ((a.dateTimeMillis - start) / (7L * 24 * 60 * 60 * 1000)).toInt().coerceIn(0, 4)
            buckets[week] += (a.fee - a.discount).coerceAtLeast(0.0)
        }
        return buckets.toList()
    }
}
