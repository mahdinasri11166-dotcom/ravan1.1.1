package com.therapist.assistant.assessment

/**
 * بانک آزمون‌های اپ — فقط تجمیع‌کننده.
 *
 * برای افزودن آزمون جدید:
 * ۱) فایل بستهٔ موضوعی مناسب را باز کنید (یا بستهٔ جدید بسازید)
 * ۲) AssessmentDef را به instruments آن بسته اضافه کنید
 * ۳) اگر facet دارید، در facetSummary همین فایل یا فایل بسته handle کنید
 * ۴) بسته را در لیست all پایین اضافه کنید
 *
 * بسته‌های فعلی:
 * - MoodAnxietyAssessments
 * - SubstanceNeuroAssessments
 * - WellbeingOutcomeAssessments
 * - PersonalitySocialAssessments
 * - ExtendedAssessments (WHODAS, LSAS, COPE, IPIP, CORE-OM)
 * - ApaFreeAssessments (PCL-5, AUDIT, K10, DSM-5 L1, ACE, BIS, ASRS)
 * - MoreFreeAssessments (DAST, FTND, SPIN, PANAS, …)
 * - ClinicalFreeAssessments (CERQ-18, Holland)
 */
object AssessmentCatalog {

    val all: List<AssessmentDef> =
        MoodAnxietyAssessments.instruments +
        SubstanceNeuroAssessments.instruments +
        WellbeingOutcomeAssessments.instruments +
        PersonalitySocialAssessments.instruments +
        ExtendedAssessments.instruments +
        ApaFreeAssessments.instruments +
        MoreFreeAssessments.instruments +
        ClinicalFreeAssessments.instruments

    fun byId(id: String): AssessmentDef? = all.find { it.id == id }

    fun search(query: String): List<AssessmentDef> {
        val q = query.trim()
        if (q.isEmpty()) return all
        return all.filter {
            it.id.contains(q, true) || it.titleFa.contains(q, true) ||
                it.titleEn.contains(q, true) || it.description.contains(q, true) ||
                it.tags.contains(q, true)
        }
    }


    /** مجموع نمره بر اساس options (مقدار Pair دوم) */
    fun score(def: AssessmentDef, answers: List<Int>): Int {
        if (answers.isEmpty()) return 0
        // answers already store selected option values
        return answers.sum()
    }

    fun facetSummary(def: AssessmentDef, answers: List<Int>): String {
        return when (def.id) {
        "TIPI" -> {
            if (answers.size < 10) return ""
            fun rev(v: Int) = 8 - v
            val e = (answers[0] + rev(answers[5])) / 2.0
            val a = (rev(answers[1]) + answers[6]) / 2.0
            val c = (answers[2] + rev(answers[7])) / 2.0
            val n = (answers[3] + rev(answers[8])) / 2.0
            val o = (answers[4] + rev(answers[9])) / 2.0
            "E=%.1f A=%.1f C=%.1f N=%.1f O=%.1f".format(e, a, c, n, o)
        }
        "BFI10" -> {
            if (answers.size < 10) return ""
            fun r(v: Int) = 6 - v
            val e = (r(answers[0]) + answers[5]) / 2.0
            val a = (answers[1] + r(answers[6])) / 2.0
            val c = (r(answers[2]) + answers[7]) / 2.0
            val n = (r(answers[3]) + answers[8]) / 2.0
            val o = (r(answers[4]) + answers[9]) / 2.0
            "E=%.1f A=%.1f C=%.1f N=%.1f O=%.1f".format(e, a, c, n, o)
        }
        "DASS21" -> {
            if (answers.size < 21) return ""
            fun sumIdx(idxs: List<Int>) = idxs.sumOf { answers[it] } * 2
            val d = sumIdx(listOf(2, 4, 9, 12, 15, 16, 20))
            val a = sumIdx(listOf(1, 3, 6, 8, 14, 18, 19))
            val s = sumIdx(listOf(0, 5, 7, 10, 11, 13, 17))
            "افسردگی=$d اضطراب=$a استرس=$s"
        }
        "PID5BF" -> {
            if (answers.size < 25) return ""
            fun avg(from: Int) = answers.subList(from, from + 5).sum() / 5.0
            "NegAffect=%.1f Detach=%.1f Antagon=%.1f Disinh=%.1f Psychoticism=%.1f".format(
                avg(0), avg(5), avg(10), avg(15), avg(20)
            )
        }
        "ECR12" -> {
            if (answers.size < 12) return ""
            "اضطراب=${answers.subList(0, 4).sum()} اجتناب=${answers.subList(4, 12).sum()}"
        }
        else -> {
            val ext = ExtendedAssessments.facetSummary(def.id, answers)
            if (ext.isNotEmpty()) ext
            else {
                val apa = ApaFreeAssessments.facetSummary(def.id, answers)
                if (apa.isNotEmpty()) apa
                else {
                    val more = MoreFreeAssessments.facetSummary(def.id, answers)
                    if (more.isNotEmpty()) more else ClinicalFreeAssessments.facetSummary(def.id, answers)
                }
            }
        }
    }
}
}
