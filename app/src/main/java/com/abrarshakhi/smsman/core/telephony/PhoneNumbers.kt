package com.abrarshakhi.smsman.core.telephony

import android.telephony.PhoneNumberUtils

/**
 * One conversation can carry the same number in several forms — a thread here holds both
 * "01521778285" (as dialled) and "+8801521778285" (as the network reported it), which rendered as
 * a title naming the same person twice.
 */
object PhoneNumbers {

    /**
     * Loose comparison is only safe for things that really are phone numbers.
     * [PhoneNumberUtils.compare] matches on trailing digits, so two alphanumeric shortcodes with no
     * digits ("GP Combo", "GovtInfo") would compare equal — which is why those fall back to an
     * exact match instead.
     */
    private const val MIN_DIGITS_FOR_LOOSE_MATCH = 7

    fun sameNumber(a: String, b: String): Boolean =
        if (isDiallable(a) && isDiallable(b)) {
            PhoneNumberUtils.compare(a, b)
        } else {
            a.equals(b, ignoreCase = true)
        }

    fun isDiallable(address: String): Boolean =
        address.count(Char::isDigit) >= MIN_DIGITS_FOR_LOOSE_MATCH

    /** Collapses variants of one number, keeping the most qualified form (+country beats local). */
    fun distinct(addresses: List<String>): List<String> {
        val result = mutableListOf<String>()
        addresses.filter { it.isNotBlank() }.forEach { candidate ->
            val index = result.indexOfFirst { sameNumber(it, candidate) }
            when {
                index < 0 -> result += candidate
                candidate.length > result[index].length -> result[index] = candidate
            }
        }
        return result
    }
}
