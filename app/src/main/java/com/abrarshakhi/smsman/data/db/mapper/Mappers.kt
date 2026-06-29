package com.abrarshakhi.smsman.data.db.mapper

import com.abrarshakhi.smsman.data.db.entity.ConversationEntity
import com.abrarshakhi.smsman.data.db.entity.MessageEntity
import com.abrarshakhi.smsman.domain.model.Conversation
import com.abrarshakhi.smsman.domain.model.Message
import com.abrarshakhi.smsman.domain.model.MessageProtocol
import com.abrarshakhi.smsman.domain.model.MessageStatus
import com.abrarshakhi.smsman.domain.model.MessageType
import com.abrarshakhi.smsman.domain.model.MmsDownloadState

fun ConversationEntity.toDomain(): Conversation = Conversation(
    threadId = threadId,
    recipientAddresses = recipientAddresses
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() },
    snippet = snippet,
    lastMessageAt = lastMessageAt,
    unreadCount = unreadCount,
    archived = archived,
    pinned = pinned,
    pinnedAt = pinnedAt,
    blocked = blocked,
    subscriptionId = subscriptionId,
    draft = draft,
    muted = muted,
)

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    threadId = threadId,
    address = address,
    body = body,
    date = date,
    type = MessageType.fromRaw(type),
    read = read,
    seen = seen,
    status = MessageStatus.fromRaw(status),
    subscriptionId = subscriptionId,
    errorCode = errorCode,
    protocol = MessageProtocol.fromRaw(protocol),
    mmsContentLocation = mmsContentLocation,
    mmsExpiry = mmsExpiry,
    mmsDownloadState = MmsDownloadState.fromRaw(mmsDownloadState),
    attachments = emptyList(),
)
