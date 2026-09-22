package com.abrarshakhi.smsman

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.abrarshakhi.smsman.core.database.SmsmanDatabase
import com.abrarshakhi.smsman.core.repository.MessageMetadataRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the generated DAO SQL, which otherwise only fails at runtime. */
@RunWith(AndroidJUnit4::class)
class MessageMetadataRepositoryTest {

    private lateinit var db: SmsmanDatabase
    private lateinit var repository: MessageMetadataRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, SmsmanDatabase::class.java).build()
        repository = MessageMetadataRepository(db.conversationMetaDao(), db.messageMetaDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun favoriteRoundTrips() = runBlocking {
        assertTrue(repository.observeFavoriteThreadIds().first().isEmpty())

        repository.setFavorite(threadId = 42L, favorite = true)
        assertEquals(setOf(42L), repository.observeFavoriteThreadIds().first())

        repository.setFavorite(threadId = 42L, favorite = false)
        assertTrue(repository.observeFavoriteThreadIds().first().isEmpty())
    }

    @Test
    fun favoriteUpsertPreservesRecipientKey() = runBlocking {
        repository.setFavorite(threadId = 7L, favorite = true, recipientKey = "+8801000000000")
        // Un-favouriting must not lose metadata that outlives the flag.
        repository.setFavorite(threadId = 7L, favorite = false)

        val row = db.conversationMetaDao().find(7L)
        assertEquals("+8801000000000", row?.recipientKey)
    }

    @Test
    fun pinRoundTripsAndIsScopedToThread() = runBlocking {
        repository.pin(messageId = 100L, threadId = 9L, date = 1L, type = 1, body = "hello")
        repository.pin(messageId = 101L, threadId = 9L, date = 2L, type = 1, body = "world")
        repository.pin(messageId = 200L, threadId = 10L, date = 3L, type = 1, body = "other")

        assertEquals(3, repository.observeAllPinned().first().size)
        assertEquals(setOf(100L, 101L), repository.observePinnedIdsInThread(9L).first())

        repository.unpin(100L)
        assertEquals(setOf(101L), repository.observePinnedIdsInThread(9L).first())
    }

    @Test
    fun pinnedAreOrderedNewestFirst() = runBlocking {
        repository.pin(messageId = 1L, threadId = 1L, date = 1L, type = 1, body = "first")
        Thread.sleep(5)
        repository.pin(messageId = 2L, threadId = 1L, date = 2L, type = 1, body = "second")

        assertEquals(listOf(2L, 1L), repository.observeAllPinned().first().map { it.messageId })
    }

    @Test
    fun pruneDropsDanglingPins() = runBlocking {
        repository.pin(messageId = 1L, threadId = 1L, date = 1L, type = 1, body = "kept")
        repository.pin(messageId = 2L, threadId = 1L, date = 2L, type = 1, body = "gone")

        repository.prunePins(listOf(2L))

        assertEquals(listOf(1L), repository.observeAllPinned().first().map { it.messageId })
    }

    @Test
    fun fingerprintIsContentDerived() = runBlocking {
        repository.pin(messageId = 1L, threadId = 5L, date = 99L, type = 1, body = "same")
        val a = db.messageMetaDao().observeAllPinned().first().single().fingerprint

        repository.unpin(1L)
        // Same content under a different provider id must produce the same fingerprint,
        // which is what lets a pin survive a backup/restore renumbering rows.
        repository.pin(messageId = 777L, threadId = 5L, date = 99L, type = 1, body = "same")
        val b = db.messageMetaDao().observeAllPinned().first().single().fingerprint

        assertEquals(a, b)
    }
}
