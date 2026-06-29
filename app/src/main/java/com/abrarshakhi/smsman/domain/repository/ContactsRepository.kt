package com.abrarshakhi.smsman.domain.repository

import com.abrarshakhi.smsman.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    suspend fun resolve(phoneNumber: String): Contact?
    fun observe(phoneNumber: String): Flow<Contact?>
    suspend fun resolveAll(phoneNumbers: Collection<String>): Map<String, Contact>
    suspend fun search(query: String): List<Contact>
}
