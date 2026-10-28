package com.therapist.assistant.util

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.Client
import com.therapist.assistant.data.QuestionnaireResult
import com.therapist.assistant.data.Workshop
import com.therapist.assistant.data.WorkshopParticipant
import com.therapist.assistant.assessment.AssessmentCatalog
import java.io.File
import java.text.NumberFormat
import java.util.Locale

/**
 * چاپ کامل پرونده مراجع — بخش‌بخش و منظم.
 */
object ClientFileExporter {

    fun exportPdf(
        file: File,
        clinicName: String,
        clinicPhone: String,
        clinicAddress: String,
        client: Client,
        appointments: List<Appointment>,
        questionnaires: List<QuestionnaireResult>,
        workshopLinks: List<Pair<Workshop, WorkshopParticipant>>
    ) {
        val currency = NumberFormat.getNumberInstance(Locale("fa"))
        val sortedAppts = appointments.sortedBy { it.dateTimeMillis }
        val completed = sortedAppts.filter { it.status == AppointmentStatus.COMPLETED }
        val cancelled = sortedAppts.filter { it.status == AppointmentStatus.CANCELLED }
        val scheduled = sortedAppts.filter { it.status == AppointmentStatus.SCHEDULED }
        val unpaid = sortedAppts.filter { !it.isPaid && (it.fee - it.discount) > 0 }
        val paidSum = sortedAppts.filter { it.isPaid }.sumOf { (it.fee - it.discount).coerceAtLeast(0.0) }

        val intro = listOf(
            "پرونده کامل مراجع",
            "تاریخ تهیه گزارش: ${JalaliDate.formatDateLong(System.currentTimeMillis())}"
        )

        // --- مشخصات ---
        val infoRows = listOf(
            listOf("نام و نام خانوادگی", client.name),
            listOf("شماره تماس", client.phone.ifBlank { "—" }),
            listOf("کد ملی", client.nationalId.ifBlank { "—" }),
            listOf("تاریخ تولد", if (client.birthDateMillis > 0) JalaliDate.formatDate(client.birthDateMillis) else "—"),
            listOf("مدرک تحصیلی", client.education.ifBlank { "—" }),
            listOf("آدرس", client.address.ifBlank { "—" }),
            listOf("وضعیت پرونده", if (client.isArchived) "بایگانی" else "فعال"),
            listOf("تاریخ و ساعت ثبت در سیستم", JalaliDate.formatDateTime(client.createdAt)),
            listOf(
                "درمانگر(ان) پرونده",
                appointments.map { it.therapistName }.filter { it.isNotBlank() }.distinct()
                    .joinToString("، ").ifBlank { "—" }
            )
        )

        // --- خلاصه درمان ---
        val summaryRows = listOf(
            listOf("کل جلسات", sortedAppts.size.toString()),
            listOf("انجام‌شده", completed.size.toString()),
            listOf("زمان‌بندی‌شده", scheduled.size.toString()),
            listOf("لغو‌شده", cancelled.size.toString()),
            listOf("جلسات بدهکار", unpaid.size.toString()),
            listOf("جمع پرداخت‌شده (تومان)", currency.format(paidSum.toLong())),
            listOf("تعداد آزمون‌ها", questionnaires.size.toString()),
            listOf("شرکت در کارگاه‌ها", workshopLinks.size.toString())
        )

        // --- جدول جلسات ---
        val sessionHeader = listOf("ردیف", "تاریخ و ساعت", "وضعیت", "مبلغ", "پرداخت", "یادداشت/SOAP")
        val sessionRows = sortedAppts.mapIndexed { i, a ->
            val soap = buildString {
                if (a.soapSubjective.isNotBlank()) append("S:${a.soapSubjective.take(40)} ")
                if (a.soapObjective.isNotBlank()) append("O:${a.soapObjective.take(40)} ")
                if (a.soapAssessment.isNotBlank()) append("A:${a.soapAssessment.take(40)} ")
                if (a.soapPlan.isNotBlank()) append("P:${a.soapPlan.take(40)}")
            }.ifBlank {
                a.sessionNotes.ifBlank { a.notes }.take(80).ifBlank { "—" }
            }
            listOf(
                (i + 1).toString(),
                JalaliDate.formatDateTime(a.dateTimeMillis),
                statusFa(a.status),
                run {
                    val net = (a.fee - a.discount).coerceAtLeast(0.0)
                    if (a.discount > 0) "${currency.format(net.toLong())} (تخفیف ${currency.format(a.discount.toLong())})"
                    else currency.format(a.fee.toLong())
                },
                if (a.isPaid) "پرداخت‌شده" else "بدهکار",
                soap
            )
        }

        // --- جزئیات SOAP / گفته‌ها (بخش متنی) ---
        val soapSections = sortedAppts.filter {
            it.soapSubjective.isNotBlank() || it.soapObjective.isNotBlank() ||
                it.soapAssessment.isNotBlank() || it.soapPlan.isNotBlank() ||
                it.sessionNotes.isNotBlank()
        }.map { a ->
            val title = "جلسه ${JalaliDate.formatDateTime(a.dateTimeMillis)} — ${statusFa(a.status)}"
            val lines = mutableListOf<String>()
            if (a.noteTags.isNotBlank()) lines += "تگ‌ها: ${a.noteTags}"
            if (a.soapSubjective.isNotBlank()) lines += "S (گفته‌های مراجع): ${a.soapSubjective}"
            if (a.soapObjective.isNotBlank()) lines += "O (مشاهدات): ${a.soapObjective}"
            if (a.soapAssessment.isNotBlank()) lines += "A (ارزیابی): ${a.soapAssessment}"
            if (a.soapPlan.isNotBlank()) lines += "P (برنامه): ${a.soapPlan}"
            if (a.sessionNotes.isNotBlank()) lines += "یادداشت آزاد: ${a.sessionNotes}"
            if (a.cancelReason.isNotBlank()) lines += "دلیل لغو/جابجایی: ${a.cancelReason}"
            title to lines
        }

        // --- آزمون‌ها ---
        val qRows = questionnaires.sortedBy { it.createdAt }.mapIndexed { i, q ->
            listOf(
                (i + 1).toString(),
                AssessmentCatalog.byId(q.type)?.titleFa ?: q.type,
                JalaliDate.formatDateTime(q.createdAt),
                q.score.toString(),
                q.detailsJson.take(60).ifBlank { "—" }
            )
        }

        // --- کارگاه‌ها ---
        val wRows = workshopLinks.mapIndexed { i, (w, p) ->
            listOf(
                (i + 1).toString(),
                w.title,
                JalaliDate.formatDateTime(w.dateMillis),
                w.trainerName.ifBlank { "—" },
                p.preTestScore.ifBlank { "—" },
                p.postTestScore.ifBlank { "—" }
            )
        }

        // یادداشت پرونده
        val noteSection = if (client.notes.isNotBlank()) {
            listOf("یادداشت پرونده" to listOf(client.notes))
        } else emptyList()


        val sections = mutableListOf<Pair<String, List<String>>>()
        val extraTables = mutableListOf<PdfExporter.NamedTable>()

        extraTables += PdfExporter.NamedTable(
            title = "۱. مشخصات فردی",
            columns = listOf(
                PdfExporter.TableColumn("عنوان", 0.38f),
                PdfExporter.TableColumn("مقدار", 0.62f)
            ),
            rows = infoRows
        )

        extraTables += PdfExporter.NamedTable(
            title = "۲. خلاصه روند درمان",
            columns = listOf(
                PdfExporter.TableColumn("شاخص", 0.55f),
                PdfExporter.TableColumn("مقدار", 0.45f)
            ),
            rows = summaryRows
        )

        soapSections.forEachIndexed { idx, pair ->
            sections += "۴.${idx + 1} روند / گفته‌های جلسه" to (listOf(pair.first) + pair.second)
        }

        if (qRows.isNotEmpty()) {
            extraTables += PdfExporter.NamedTable(
                title = "۵. آزمون‌های روان‌شناختی",
                columns = listOf(
                    PdfExporter.TableColumn("ردیف", 0.10f),
                    PdfExporter.TableColumn("آزمون", 0.28f),
                    PdfExporter.TableColumn("تاریخ و ساعت", 0.22f),
                    PdfExporter.TableColumn("نمره", 0.12f),
                    PdfExporter.TableColumn("جزئیات", 0.28f)
                ),
                rows = qRows
            )
        }

        if (questionnaires.isNotEmpty()) {
            val byType = questionnaires.groupBy { it.type }
            val trendLines = mutableListOf<String>()
            byType.forEach { (type, list) ->
                val sorted = list.sortedBy { it.createdAt }
                val titleFa = AssessmentCatalog.byId(type)?.titleFa ?: type
                val pts = sorted.joinToString(" ← ") {
                    "${JalaliDate.formatDate(it.createdAt)}=${it.score}"
                }
                val delta = if (sorted.size >= 2) sorted.last().score - sorted.first().score else 0
                val deltaText = when {
                    sorted.size < 2 -> "یک نوبت"
                    delta > 0 -> "افزایش $delta"
                    delta < 0 -> "کاهش ${-delta}"
                    else -> "بدون تغییر"
                }
                trendLines += "$titleFa: $pts  ($deltaText)"
            }
            if (trendLines.isNotEmpty()) {
                sections += "۵ب. شرح روند نمرات آزمون‌ها" to trendLines
            }
        }

        if (wRows.isNotEmpty()) {
            extraTables += PdfExporter.NamedTable(
                title = "۶. کارگاه‌های آموزشی",
                columns = listOf(
                    PdfExporter.TableColumn("ردیف", 0.08f),
                    PdfExporter.TableColumn("عنوان", 0.28f),
                    PdfExporter.TableColumn("تاریخ", 0.18f),
                    PdfExporter.TableColumn("آموزشگر", 0.18f),
                    PdfExporter.TableColumn("پیش‌آزمون", 0.14f),
                    PdfExporter.TableColumn("پس‌آزمون", 0.14f)
                ),
                rows = wRows
            )
        }

        sections.addAll(noteSection.map { "۷. ${it.first}" to it.second })
        sections += "توضیح" to listOf(
            "این پرونده توسط نرم‌افزار درمانگرنو تولید شده و محرمانه است.",
            "محتوای بالینی فقط برای استفاده درمانگر و با رعایت اصول اخلاقی قابل اشتراک است."
        )

        val chartColors = listOf(
            0xFF0284C7.toInt(),
            0xFF059669.toInt(),
            0xFF7C3AED.toInt(),
            0xFFEA580C.toInt(),
            0xFFDB2777.toInt(),
            0xFF0D9488.toInt(),
            0xFFCA8A04.toInt(),
            0xFF4F46E5.toInt()
        )
        val chartSeries = questionnaires
            .groupBy { it.type }
            .entries
            .mapIndexed { index, (type, list) ->
                val sorted = list.sortedBy { it.createdAt }.takeLast(12)
                PdfExporter.ChartSeries(
                    label = AssessmentCatalog.byId(type)?.titleFa ?: type,
                    scores = sorted.map { it.score },
                    color = chartColors[index % chartColors.size]
                )
            }
            .filter { it.scores.isNotEmpty() }
            .sortedByDescending { it.scores.size }

        PdfExporter.writeStyledPdf(
            file = file,
            brand = PdfExporter.Brand(
                title = clinicName.ifBlank { "مرکز گل سرخ" },
                subtitle = "پرونده کامل — ${client.name}",
                phone = clinicPhone,
                address = clinicAddress
            ),
            introLines = intro,
            tableColumns = listOf(
                PdfExporter.TableColumn("ردیف", 0.08f),
                PdfExporter.TableColumn("تاریخ و ساعت", 0.20f),
                PdfExporter.TableColumn("وضعیت", 0.12f),
                PdfExporter.TableColumn("مبلغ", 0.12f),
                PdfExporter.TableColumn("پرداخت", 0.12f),
                PdfExporter.TableColumn("یادداشت / SOAP", 0.36f)
            ),
            tableRows = sessionRows.ifEmpty {
                listOf(listOf("—", "جلسه‌ای ثبت نشده", "—", "—", "—", "—"))
            },
            extraTables = extraTables,
            sections = sections,
            chartSeries = chartSeries,
            chartTitle = "نمودار رنگی روند تغییر نمرات آزمون‌ها",
            footer = "محرمانه — $clinicName — درمانگرنو"
        )
    }

    private fun statusFa(s: AppointmentStatus): String = when (s) {
        AppointmentStatus.SCHEDULED -> "زمان‌بندی"
        AppointmentStatus.COMPLETED -> "انجام‌شده"
        AppointmentStatus.CANCELLED -> "لغو"
        AppointmentStatus.RESCHEDULED -> "جابجا"
    }
}
