package com.abrarshakhi.smsman.core.telephony

import android.database.Cursor

/**
 * The Telephony provider is OEM-customised (this device returns vendor columns such as
 * `oplus_unread_count` and `rcs_top` that do not exist elsewhere), so every column read must
 * tolerate a missing column rather than assume a fixed cursor layout.
 */

internal fun Cursor.longOr(name: String, fallback: Long = 0L): Long {
    val index = getColumnIndex(name)
    return if (index < 0 || isNull(index)) fallback else getLong(index)
}

internal fun Cursor.intOr(name: String, fallback: Int = 0): Int {
    val index = getColumnIndex(name)
    return if (index < 0 || isNull(index)) fallback else getInt(index)
}

internal fun Cursor.stringOrNull(name: String): String? {
    val index = getColumnIndex(name)
    return if (index < 0 || isNull(index)) null else getString(index)
}
