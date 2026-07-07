package com.abrarshakhi.smsman.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")

/**
 * Tracks one-time onboarding completion. Stored in DataStore so it survives
 * configuration changes and app upgrades. Cleared by "clear app data".
 */
@Singleton
class OnboardingTracker @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun observeCompleted(): Flow<Boolean> =
        context.onboardingDataStore.data.map { it[KEY_COMPLETED] ?: false }

    suspend fun markCompleted() {
        context.onboardingDataStore.edit { it[KEY_COMPLETED] = true }
    }

    private companion object {
        val KEY_COMPLETED = booleanPreferencesKey("completed")
    }
}
