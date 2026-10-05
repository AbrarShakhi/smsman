package com.abrarshakhi.smsman.data.repository

import com.abrarshakhi.smsman.data.provider.ContactsDataSource
import com.abrarshakhi.smsman.data.provider.MessagesDataSource
import com.abrarshakhi.smsman.data.provider.PhoneNumbers
import com.abrarshakhi.smsman.model.ThreadTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThreadTitleResolver(
    private val messages: MessagesDataSource,
    private val contacts: ContactsDataSource,
) {

    suspend fun resolve(threadId: Long): ThreadTitle = withContext(Dispatchers.IO) {
        val addresses = PhoneNumbers.distinct(messages.threadAddresses(threadId))
        if (addresses.isEmpty()) return@withContext ThreadTitle.Unknown

        val named = addresses.map { address -> address to contacts.lookup(address)?.displayName }
        val title = named.joinToString(", ") { (address, name) -> name ?: address }

        val subtitle = named.singleOrNull()
            ?.let { (address, name) -> address.takeIf { name != null } }

        ThreadTitle(title = title, subtitle = subtitle)
    }
}
