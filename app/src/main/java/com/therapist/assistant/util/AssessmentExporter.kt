package com.therapist.assistant.util

import com.therapist.assistant.assessment.AssessmentCatalog
import com.therapist.assistant.assessment.AssessmentDef
import java.io.File

object AssessmentExporter {

    fun buildPdf(
        file: File,
        clinicName: String,
        clinicPhone: String,
        clinicAddress: String,
        clientName: String,
        def: AssessmentDef,
        answers: List<Int>,
        total: Int,
        interpretation: String,
        dateText: String
    ) {
        val facets = AssessmentCatalog.facetSummary(def, answers)
        val intro = listOf(
            "مراجع: $clientName",
            "تاریخ اجرا: $dateText",
            "آزمون: ${def.titleFa}",
            "عنوان انگلیسی: ${def.titleEn}",
            "نمره کل: $total",
            "تفسیر: $interpretation"
        ) + if (facets.isNotBlank()) listOf("خرده مقیاس‌ها: $facets") else emptyList()

        val columns = listOf(
            PdfExporter.TableColumn("ردیف", 0.12f),
            PdfExporter.TableColumn("سؤال", 0.55f),
            PdfExporter.TableColumn("نمره", 0.12f),
            PdfExporter.TableColumn("پاسخ", 0.21f)
        )
        val rows = def.items.mapIndexed { i, q ->
            val a = answers.getOrNull(i) ?: -1
            val label = def.options.find { it.second == a }?.first ?: "—"
            listOf((i + 1).toString(), q, a.toString(), label)
        }

        PdfExporter.writeStyledPdf(
            file = file,
            brand = PdfExporter.Brand(
                title = clinicName.ifBlank { "درمانگرنو" },
                subtitle = "گزارش آزمون روان‌شناختی — ${def.id}",
                phone = clinicPhone,
                address = clinicAddress
            ),
            introLines = intro,
            tableColumns = columns,
            tableRows = rows,
            sections = listOf(
                "توضیح" to listOf(
                    def.description,
                    "این خروجی ابزار غربالگری است و جایگزین تشخیص بالینی تخصصی نیست."
                )
            ),
            footer = "محرمانه — $clinicName — درمانگرنو"
        )
    }

    fun buildXlsx(
        file: File,
        clinicName: String,
        clinicPhone: String,
        clinicAddress: String,
        clientName: String,
        def: AssessmentDef,
        answers: List<Int>,
        total: Int,
        interpretation: String,
        dateText: String
    ) {
        val facets = AssessmentCatalog.facetSummary(def, answers)
        val cover = listOf(
            listOf("گزارش آزمون روان‌شناختی"),
            listOf("مرکز", clinicName),
            listOf("تلفن", clinicPhone),
            listOf("آدرس", clinicAddress),
            listOf("تاریخ", dateText),
            listOf("مراجع", clientName),
            listOf("کد آزمون", def.id),
            listOf("عنوان فارسی", def.titleFa),
            listOf("عنوان انگلیسی", def.titleEn),
            listOf("نمره کل", total.toString()),
            listOf("تفسیر", interpretation),
            listOf("خرده مقیاس‌ها", facets),
            listOf(),
            listOf("— جدول پاسخ‌ها —"),
            listOf("ردیف", "سؤال", "نمره", "برچسب پاسخ")
        )
        val answerRows = def.items.mapIndexed { i, q ->
            val a = answers.getOrNull(i) ?: -1
            val label = def.options.find { it.second == a }?.first ?: ""
            listOf((i + 1).toString(), q, a.toString(), label)
        }
        val summary = listOf(
            listOf("خلاصه"),
            listOf("تعداد سؤالات", def.items.size.toString()),
            listOf("نمره کل", total.toString()),
            listOf("تفسیر", interpretation),
            listOf("خرده مقیاس‌ها", facets)
        )
        val headerIdx = cover.size  // 1-based row of table header
        XlsxWriter.write(
            file,
            listOf(
                XlsxWriter.Sheet(
                    name = "گزارش آزمون",
                    rows = cover + answerRows,
                    headerRowIndices = setOf(headerIdx),
                    colWidths = listOf(8.0, 48.0, 10.0, 18.0)
                ),
                XlsxWriter.Sheet(
                    name = "خلاصه",
                    rows = summary,
                    headerRowIndices = setOf(1),
                    colWidths = listOf(18.0, 40.0)
                )
            )
        )
    }
}
