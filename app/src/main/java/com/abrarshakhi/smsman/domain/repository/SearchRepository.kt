package com.abrarshakhi.smsman.domain.repository

import com.abrarshakhi.smsman.domain.model.SearchHit

interface SearchRepository {
    suspend fun searchMessages(query: String, limit: Int = 200): List<SearchHit>
    suspend fun searchInThread(threadId: Long, query: String): List<SearchHit>
}
