package com.therapist.assistant

import com.therapist.assistant.data.LicensePlan
import org.junit.Assert.assertTrue
import org.junit.Test

class LicensePlanTest {
    @Test
    fun freeLimitIsPositive() {
        val free = LicensePlan.resolve(isPro = false, isOrg = false, trialActive = false)
        assertTrue(free.clientLimit > 0)
    }

    @Test
    fun proHasHighLimit() {
        val pro = LicensePlan.resolve(isPro = true, isOrg = false, trialActive = false)
        assertTrue(pro.clientLimit >= 50)
    }
}