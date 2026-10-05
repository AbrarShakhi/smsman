package com.abrarshakhi.smsman.sms

import android.telephony.SmsMessage

data class SegmentInfo(
    val segments: Int,
    val remainingInSegment: Int,
    val isUnicode: Boolean,
)

fun segmentInfo(text: String): SegmentInfo {
    if (text.isEmpty()) return SegmentInfo(0, 0, false)
    val calculated = SmsMessage.calculateLength(text, false)
    return SegmentInfo(
        segments = calculated[0],
        remainingInSegment = calculated[2],
        isUnicode = calculated[3] == ENCODING_16BIT,
    )
}

private const val ENCODING_16BIT = 3
