package com.therapist.assistant.data

/**
 * پلن‌های تجاری برنامه — نمایش و سقف‌ها.
 * فعال‌سازی کامل Pro/Org با کد؛ آزمایشی ۱۴روزه خودکار.
 */
enum class LicensePlan(
    val id: String,
    val titleFa: String,
    val clientLimit: Int,
    val therapistsLimit: Int,
    val allAssessments: Boolean,
    val cloudBackupShare: Boolean,
    val orgReports: Boolean
) {
    FREE(
        id = "free",
        titleFa = "رایگان",
        clientLimit = 50,
        therapistsLimit = 1,
        allAssessments = false,
        cloudBackupShare = false,
        orgReports = false
    ),
    TRIAL(
        id = "trial",
        titleFa = "آزمایشی ۱۴ روزه",
        clientLimit = Int.MAX_VALUE,
        therapistsLimit = 5,
        allAssessments = true,
        cloudBackupShare = true,
        orgReports = true
    ),
    PRO(
        id = "pro",
        titleFa = "حرفه‌ای (فردی)",
        clientLimit = Int.MAX_VALUE,
        therapistsLimit = 3,
        allAssessments = true,
        cloudBackupShare = true,
        orgReports = false
    ),
    ORG(
        id = "org",
        titleFa = "سازمانی (کلینیک)",
        clientLimit = Int.MAX_VALUE,
        therapistsLimit = 50,
        allAssessments = true,
        cloudBackupShare = true,
        orgReports = true
    );

    companion object {
        fun resolve(isPro: Boolean, isOrg: Boolean, trialActive: Boolean): LicensePlan = when {
            isOrg -> ORG
            isPro -> PRO
            trialActive -> TRIAL
            else -> FREE
        }
    }
}

/** بسته‌های آزمون — نسخه رایگان فقط غربالگری پایه */
object AssessmentPacks {
    val SCREENING = setOf(
        "PHQ9", "GAD7", "K6", "PSS10", "WHO5",
        "AUDIT_C", "CAGE", "SCOFF", "ISI", "GDS15"
    )
    val MOOD_ANXIETY = setOf("PHQ9", "GAD7", "DASS21", "BAI", "BDI2", "HADS", "PSS10", "K6")
    val SLEEP_SUBSTANCE = setOf("ISI", "PSQI", "AUDIT10", "AUDIT_C", "CAGE", "DAST10")
    val PERSONALITY = setOf("TIPI", "BFI10", "PID5BF", "ECR12")

    fun packTitle(id: String): String = when (id) {
        "screening" -> "غربالگری پایه (رایگان)"
        "mood" -> "خلق و اضطراب"
        "sleep" -> "خواب و مواد"
        "personality" -> "شخصیت و دلبستگی"
        else -> id
    }
}
