package com.abrarshakhi.smsman.sms

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmsSegmentsTest {

    @Test
    fun emptyTextHasNoSegments() {
        assertEquals(0, segmentInfo("").segments)
    }

    @Test
    fun shortAsciiIsOneGsm7Segment() {
        val info = segmentInfo("hello")
        assertEquals(1, info.segments)
        assertFalse(info.isUnicode)
    }

    @Test
    fun asciiSplitsAt160() {
        assertEquals(1, segmentInfo("a".repeat(160)).segments)
        assertEquals(2, segmentInfo("a".repeat(161)).segments)
    }

    @Test
    fun bengaliIsUnicodeAndSplitsAt70() {
        val bengali = "আ"
        val single = segmentInfo(bengali.repeat(70))
        assertTrue("Bengali must be detected as UCS-2", single.isUnicode)
        assertEquals(1, single.segments)

        assertEquals(2, segmentInfo(bengali.repeat(71)).segments)
        assertEquals(1, segmentInfo("a".repeat(71)).segments)
    }

    @Test
    fun aSingleBengaliCharacterForcesUnicodeForTheWholeMessage() {
        val mixed = "a".repeat(100) + "আ"
        val info = segmentInfo(mixed)
        assertTrue(info.isUnicode)
        assertEquals(2, info.segments)
    }
}
