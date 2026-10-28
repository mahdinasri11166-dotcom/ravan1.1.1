package com.therapist.assistant.util

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import java.io.File
import java.util.Calendar

object FinanceExcelExporter {
    fun writeMonthly(file: File, year: Int, month: Int, clinic: String, list: List<Appointment>) {
        val cal = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        val rows = list.filter {
            it.dateTimeMillis in start until end && it.status != AppointmentStatus.CANCELLED
        }
        val paid = rows.filter { it.isPaid }.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
        val unpaid = rows.filter { !it.isPaid }.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }
        val sheetRows = mutableListOf<List<String>>()
        sheetRows.add(listOf("گزارش مالی ماهانه", clinic, "$year/${month + 1}"))
        sheetRows.add(listOf("وصول", paid.toLong().toString(), "بدهی", unpaid.toLong().toString(), "تعداد", rows.size.toString()))
        sheetRows.add(listOf())
        sheetRows.add(listOf("مراجع", "تاریخ", "مبلغ", "تخفیف", "خالص", "وضعیت", "روش پرداخت"))
        rows.sortedBy { it.dateTimeMillis }.forEach {
            val net = (it.fee - it.discount).coerceAtLeast(0.0)
            sheetRows.add(
                listOf(
                    it.clientName,
                    JalaliDate.formatDateTime(it.dateTimeMillis),
                    it.fee.toLong().toString(),
                    it.discount.toLong().toString(),
                    net.toLong().toString(),
                    if (it.isPaid) "پرداخت" else "بدهکار",
                    it.paymentMethod.name
                )
            )
        }
        XlsxWriter.write(
            file,
            listOf(
                XlsxWriter.Sheet(
                    name = "مالی",
                    rows = sheetRows,
                    headerRowIndices = setOf(0, 3),
                    colWidths = listOf(22.0, 18.0, 12.0, 10.0, 12.0, 12.0, 14.0)
                )
            )
        )
    }
}
