package com.abrarshakhi.smsman

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abrarshakhi.smsman.core.telephony.PhoneNumbers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhoneNumbersTest {

    @Test
    fun localAndInternationalFormsAreTheSameNumber() {
        assertTrue(PhoneNumbers.sameNumber("01521778285", "+8801521778285"))
    }

    @Test
    fun distinctKeepsTheMostQualifiedForm() {
        assertEquals(
            listOf("+8801521778285"),
            PhoneNumbers.distinct(listOf("01521778285", "+8801521778285")),
        )
    }

    @Test
    fun differentNumbersAreKept() {
        val addresses = listOf("+8801521778285", "+8809648731555")
        assertEquals(2, PhoneNumbers.distinct(addresses).size)
    }

    @Test
    fun alphanumericShortcodesAreNotLooselyCompared() {
        // The trap: both have no digits, so PhoneNumberUtils.compare would call them equal.
        assertFalse(PhoneNumbers.sameNumber("GP Combo", "GovtInfo"))
        assertEquals(2, PhoneNumbers.distinct(listOf("GP Combo", "GovtInfo")).size)
    }

    @Test
    fun shortNumericCodesAreNotLooselyCompared() {
        assertFalse(PhoneNumbers.sameNumber("16216", "16672"))
        assertTrue(PhoneNumbers.sameNumber("16216", "16216"))
    }

    @Test
    fun blankAddressesAreDropped() {
        assertEquals(listOf("GP"), PhoneNumbers.distinct(listOf("", "GP", "  ".trim())))
    }
}
