
package com.therapist.assistant

import com.therapist.assistant.data.LicensePlan
import com.therapist.assistant.util.ClinicalWorksheets
import com.therapist.assistant.util.MessageHelper
import com.therapist.assistant.util.ProtocolCatalog
import org.junit.Assert.*
import org.junit.Test

class CoreLogicTest {
    @Test
    fun licenseFreeLimitIs50() {
        val p = LicensePlan.resolve(isPro = false, isOrg = false, trialActive = false)
        assertEquals(50, p.clientLimit)
    }

    @Test
    fun protocolCatalogNotEmpty() {
        assertTrue(ProtocolCatalog.all().isNotEmpty())
        assertNotNull(ProtocolCatalog.find("اضطراب (CBT)"))
    }

    @Test
    fun messageHelperReplacesTokens() {
        val t = MessageHelper.buildReminderText(
            "{name} / {clinic} / {date} / {time}",
            "علی",
            1_700_000_000_000L,
            "مرکز گل سرخ"
        )
        assertTrue(t.contains("علی"))
        assertTrue(t.contains("مرکز گل سرخ"))
        assertFalse(t.contains("{name}"))
    }

    @Test
    fun clinicalWorksheetsRoundTripSafety() {
        val s = ClinicalWorksheets.safetyPlan("a", "b", "c", "d", "e")
        assertNotNull(ClinicalWorksheets.extractSafetyPlan(s))
    }

    @Test
    fun soapDraftHasSections() {
        val p = ProtocolCatalog.all().first()
        val d = ProtocolCatalog.formatSoapDraft(p, setOf(0))
        assertTrue(d.subjective.isNotBlank())
        assertTrue(d.plan.isNotBlank())
    }
}
