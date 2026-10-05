package com.abrarshakhi.smsman.core.repository

import com.abrarshakhi.smsman.core.telephony.ContactsDataSource
import com.abrarshakhi.smsman.core.telephony.MessagesDataSource
import com.abrarshakhi.smsman.core.telephony.PhoneNumbers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ThreadTitle(
    val title: String,
    val subtitle: String?,
) {
    companion object {
        val Unknown = ThreadTitle(title = "Conversation", subtitle = null)
    }
}

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
