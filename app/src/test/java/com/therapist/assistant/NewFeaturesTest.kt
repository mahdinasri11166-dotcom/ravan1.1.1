package com.therapist.assistant

import com.therapist.assistant.data.Appointment
import com.therapist.assistant.data.AppointmentStatus
import com.therapist.assistant.data.QuestionnaireResult
import com.therapist.assistant.util.AssessmentTrends
import com.therapist.assistant.util.EncryptedBackup
import com.therapist.assistant.util.SlotFinder
import com.therapist.assistant.util.SoapTemplates
import com.therapist.assistant.util.TherapistWellness
import org.junit.Assert.*
import org.junit.Test

class NewFeaturesTest {

    @Test
    fun soapTemplatesNotEmpty() {
        assertTrue(SoapTemplates.all.size >= 5)
        assertNotNull(SoapTemplates.byId("anxiety_cbt"))
        assertTrue(SoapTemplates.formatForNotes(SoapTemplates.all.first()).contains("SOAP"))
    }

    @Test
    fun slotFinderSuggestsFutureSlots() {
        val now = System.currentTimeMillis()
        val busy = listOf(
            Appointment(
                clientId = 1,
                clientName = "آزمون",
                dateTimeMillis = now + 2 * 60 * 60 * 1000L,
                status = AppointmentStatus.SCHEDULED
            )
        )
        val slots = SlotFinder.suggest(busy, fromMillis = now, maxResults = 5)
        assertTrue(slots.isNotEmpty())
        assertTrue(slots.all { it.startMillis >= now })
    }

    @Test
    fun assessmentTrendsDetectImprovement() {
        val clientId = 7L
        val base = System.currentTimeMillis()
        val results = listOf(
            QuestionnaireResult(clientId = clientId, type = "PHQ9", score = 18, createdAt = base - 20L * 86400000),
            QuestionnaireResult(clientId = clientId, type = "PHQ9", score = 11, createdAt = base - 10L * 86400000),
            QuestionnaireResult(clientId = clientId, type = "PHQ9", score = 7, createdAt = base)
        )
        val trends = AssessmentTrends.forClient(results, clientId)
        assertEquals(1, trends.size)
        assertEquals(-4, trends.first().delta)
        assertEquals(true, trends.first().improving)
        assertTrue(AssessmentTrends.sparkline(trends.first().points).isNotBlank())
    }

    @Test
    fun encryptedBackupRoundTrip() {
        val plain = """{"app":"درمانگرنو","version":5,"clients":[]}"""
        val enc = EncryptedBackup.encrypt(plain, "test-pass")
        assertTrue(EncryptedBackup.isEncrypted(enc))
        val back = EncryptedBackup.decrypt(enc, "test-pass")
        assertEquals(plain, back)
    }

    @Test
    fun encryptedBackupWrongPasswordFails() {
        val enc = EncryptedBackup.encrypt("{}", "good-pass")
        try {
            EncryptedBackup.decrypt(enc, "bad-pass")
            fail("expected failure")
        } catch (_: Exception) {
            // ok
        }
    }

    @Test
    fun wellnessWeekStatsRuns() {
        val stats = TherapistWellness.weekStats(emptyList())
        assertEquals(0, stats.completedCount)
        assertTrue(stats.suggestionFa.isNotBlank())
    }
}
