package com.abrarshakhi.smsman.domain.repository

import com.abrarshakhi.smsman.domain.model.BlockedNumber
import kotlinx.coroutines.flow.Flow

interface BlockedNumberRepository {
    fun observeBlocked(): Flow<List<BlockedNumber>>
    suspend fun isBlocked(phoneE164: String): Boolean
    suspend fun block(phoneE164: String, reason: String?)
    suspend fun unblock(phoneE164: String)
}
