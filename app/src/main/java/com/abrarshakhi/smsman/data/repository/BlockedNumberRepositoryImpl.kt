package com.abrarshakhi.smsman.data.repository

import com.abrarshakhi.smsman.data.db.dao.BlockedNumberDao
import com.abrarshakhi.smsman.data.db.entity.BlockedNumberEntity
import com.abrarshakhi.smsman.domain.model.BlockedNumber
import com.abrarshakhi.smsman.domain.repository.BlockedNumberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedNumberRepositoryImpl @Inject constructor(
    private val dao: BlockedNumberDao,
) : BlockedNumberRepository {

    override fun observeBlocked(): Flow<List<BlockedNumber>> =
        dao.observeAll().map { list ->
            list.map { BlockedNumber(it.phoneNumberE164, it.blockedAt, it.reason) }
        }

    override suspend fun isBlocked(phoneE164: String): Boolean =
        dao.isBlocked(phoneE164)

    override suspend fun block(phoneE164: String, reason: String?) {
        dao.upsert(
            BlockedNumberEntity(
                phoneNumberE164 = phoneE164,
                blockedAt = System.currentTimeMillis(),
                reason = reason,
            ),
        )
    }

    override suspend fun unblock(phoneE164: String) = dao.unblock(phoneE164)
}
