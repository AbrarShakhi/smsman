package com.abrarshakhi.smsman.data.provider

import android.telephony.PhoneNumberUtils

object PhoneNumbers {

    private const val MIN_DIGITS_FOR_LOOSE_MATCH = 7

    fun sameNumber(a: String, b: String): Boolean =
        if (isDiallable(a) && isDiallable(b)) {
            PhoneNumberUtils.compare(a, b)
        } else {
            a.equals(b, ignoreCase = true)
        }

    fun isDiallable(address: String): Boolean =
        address.count(Char::isDigit) >= MIN_DIGITS_FOR_LOOSE_MATCH

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
