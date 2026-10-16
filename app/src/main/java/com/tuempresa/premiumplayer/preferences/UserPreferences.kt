package com.tuempresa.premiumplayer.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
        val LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        val VISUALIZER_ENABLED = booleanPreferencesKey("visualizer_enabled")
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data
        .map { it[DARK_THEME] ?: true }

    val visualizerEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[VISUALIZER_ENABLED] ?: true }

    val lastPlayedSongId: Flow<Long> = context.dataStore.data
        .map { it[LAST_PLAYED_SONG_ID] ?: -1L }

    val lastPlayedPosition: Flow<Long> = context.dataStore.data
        .map { it[LAST_PLAYED_POSITION] ?: 0L }

    val shuffleEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[SHUFFLE_ENABLED] ?: false }

    val repeatMode: Flow<Int> = context.dataStore.data
        .map { it[REPEAT_MODE] ?: 0 }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[DARK_THEME] = enabled }
    }

    suspend fun setVisualizerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VISUALIZER_ENABLED] = enabled }
    }

    suspend fun setShuffleEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SHUFFLE_ENABLED] = enabled }
    }

    suspend fun setRepeatMode(mode: Int) {
        context.dataStore.edit { it[REPEAT_MODE] = mode }
    }

    suspend fun setLastPlayed(songId: Long, positionMs: Long) {
        context.dataStore.edit {
            it[LAST_PLAYED_SONG_ID] = songId
            it[LAST_PLAYED_POSITION] = positionMs
        }
    }
}
