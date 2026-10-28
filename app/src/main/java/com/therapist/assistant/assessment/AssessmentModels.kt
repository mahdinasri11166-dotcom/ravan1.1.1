package com.therapist.assistant.assessment

/**
 * مدل مشترک بانک آزمون.
 * هر بستهٔ موضوعی (MoodAnxiety، …) لیست AssessmentDef خود را می‌دهد؛
 * [AssessmentCatalog] همه را جمع می‌کند.
 */
data class AssessmentDef(
    val id: String,
    val titleFa: String,
    val titleEn: String,
    val description: String,
    val options: List<Pair<String, Int>>,
    val items: List<String>,
    val interpret: (Int) -> String,
    val tags: String = ""
)

/** مقیاس‌های پاسخ مشترک — برای سبک‌شدن فایل‌های بسته */
object AssessmentScales {
    val likert0to3 = listOf(
        "اصلاً / هیچ‌وقت" to 0,
        "چند روز" to 1,
        "بیش از نیمی از روزها" to 2,
        "تقریباً هر روز" to 3
    )
}
