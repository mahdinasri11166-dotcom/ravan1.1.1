package com.therapist.assistant.util

import com.therapist.assistant.data.QuestionnaireResult

/**
 * محاسبه روند نمرات آزمون برای یک مراجع.
 */
object AssessmentTrends {

    data class ScorePoint(
        val millis: Long,
        val score: Int,
        val type: String
    )

    data class TrendSummary(
        val type: String,
        val points: List<ScorePoint>,
        val latest: Int?,
        val previous: Int?,
        val delta: Int?,
        val improving: Boolean?
    )

    fun forClient(
        results: List<QuestionnaireResult>,
        clientId: Long,
        type: String? = null
    ): List<TrendSummary> {
        val filtered = results
            .filter { it.clientId == clientId }
            .filter { type == null || it.type.equals(type, ignoreCase = true) }
            .sortedBy { it.createdAt }

        return filtered.groupBy { it.type }.map { (t, list) ->
            val points = list.map { ScorePoint(it.createdAt, it.score, it.type) }
            val latest = points.lastOrNull()?.score
            val previous = points.dropLast(1).lastOrNull()?.score
            val delta = if (latest != null && previous != null) latest - previous else null
            // برای اکثر مقیاس‌های علائم، کاهش = بهبود
            val improving = delta?.let { it < 0 }
            TrendSummary(t, points, latest, previous, delta, improving)
        }.sortedByDescending { it.points.lastOrNull()?.millis ?: 0L }
    }

    fun sparkline(points: List<ScorePoint>, width: Int = 12): String {
        if (points.isEmpty()) return "—"
        val scores = points.map { it.score }
        val min = scores.minOrNull() ?: 0
        val max = scores.maxOrNull() ?: 0
        val range = (max - min).coerceAtLeast(1)
        val bars = listOf('▁', '▂', '▃', '▄', '▅', '▆', '▇', '█')
        return points.takeLast(width).joinToString("") { p ->
            val idx = ((p.score - min) * (bars.size - 1) / range).coerceIn(0, bars.size - 1)
            bars[idx].toString()
        }
    }

    fun deltaLabelFa(delta: Int?): String = when {
        delta == null -> "داده کافی نیست"
        delta < 0 -> "بهبود ${JalaliDate.toFa((-delta).toString())} نمره"
        delta > 0 -> "افزایش ${JalaliDate.toFa(delta.toString())} نمره"
        else -> "بدون تغییر"
    }
}
