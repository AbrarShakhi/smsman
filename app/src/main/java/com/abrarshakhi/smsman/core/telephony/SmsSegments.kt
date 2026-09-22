package com.abrarshakhi.smsman.core.telephony

import android.telephony.SmsMessage

data class SegmentInfo(
    val segments: Int,
    val remainingInSegment: Int,
    val isUnicode: Boolean,
)

/**
 * Segment maths must come from the platform, never `length / 160`.
 *
 * Bengali (and any non-GSM-7 script) forces UCS-2, which is **70 characters per segment, not 160**.
 * This inbox is largely Bengali, so a hardcoded 160 would mis-split real messages.
 */
fun segmentInfo(text: String): SegmentInfo {
    if (text.isEmpty()) return SegmentInfo(0, 0, false)
    // int[]{ messageCount, codeUnitCount, codeUnitsRemaining, encodingType }
    val calculated = SmsMessage.calculateLength(text, false)
    return SegmentInfo(
        segments = calculated[0],
        remainingInSegment = calculated[2],
        isUnicode = calculated[3] == ENCODING_16BIT,
    )
}

private const val ENCODING_16BIT = 3
