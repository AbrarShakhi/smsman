package com.abrarshakhi.smsman.core.repository

import com.abrarshakhi.smsman.core.telephony.ContactsDataSource
import com.abrarshakhi.smsman.core.telephony.MessagesDataSource
import com.abrarshakhi.smsman.core.telephony.PhoneNumbers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

data class ThreadTitle(
    val title: String,
    val subtitle: String?,
)

/**
 * Titles the chat top bar. Lives outside the ChatViewModel because the top bar is rendered by
 * AppRoot's single Scaffold, which sits outside the NavEntry's ViewModel store and so cannot share
 * the screen's ViewModel instance.
 */
class ThreadTitleResolver(
    private val messages: MessagesDataSource,
    private val contacts: ContactsDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun resolve(threadId: Long): ThreadTitle = withContext(ioDispatcher) {
        val addresses = PhoneNumbers.distinct(messages.threadAddresses(threadId))
        if (addresses.isEmpty()) return@withContext ThreadTitle("Conversation", null)

        val named = addresses.map { address -> address to contacts.lookup(address)?.displayName }
        val title = named.joinToString(", ") { (address, name) -> name ?: address }

        // Only show the number underneath when it adds something the title does not already say.
        val subtitle = named.singleOrNull()
            ?.let { (address, name) -> address.takeIf { name != null } }

        ThreadTitle(title = title, subtitle = subtitle)
    }
}
