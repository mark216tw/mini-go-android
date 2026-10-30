package com.vibecode.minigo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.engine.Stone
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val showCoordinates: Boolean = true,
    val vibrate: Boolean = true,
    val defaultBoardSize: Int = 9,
    val defaultLevel: AiLevel = AiLevel.EASY,
    val defaultMode: GameMode = GameMode.AI,
    val defaultHumanColor: Stone = Stone.BLACK,
    val hasSeenTutorial: Boolean = false,
)

class SettingsRepository private constructor(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val defaults = AppSettings()
            AppSettings(
                themeMode = preferences[KEY_THEME]
                    ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                    ?: defaults.themeMode,
                dynamicColor = preferences[KEY_DYNAMIC_COLOR] ?: defaults.dynamicColor,
                showCoordinates = preferences[KEY_COORDINATES] ?: defaults.showCoordinates,
                vibrate = preferences[KEY_VIBRATE] ?: defaults.vibrate,
                defaultBoardSize = preferences[KEY_BOARD_SIZE]
                    ?.takeIf { it == 9 || it == 13 }
                    ?: defaults.defaultBoardSize,
                defaultLevel = preferences[KEY_LEVEL]
                    ?.let { runCatching { AiLevel.valueOf(it) }.getOrNull() }
                    ?: defaults.defaultLevel,
                defaultMode = preferences[KEY_MODE]
                    ?.let { runCatching { GameMode.valueOf(it) }.getOrNull() }
                    ?: defaults.defaultMode,
                defaultHumanColor = preferences[KEY_HUMAN_COLOR]
                    ?.let { runCatching { Stone.valueOf(it) }.getOrNull() }
                    ?: defaults.defaultHumanColor,
                hasSeenTutorial = preferences[KEY_SEEN_TUTORIAL] ?: defaults.hasSeenTutorial,
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setShowCoordinates(enabled: Boolean) {
        dataStore.edit { it[KEY_COORDINATES] = enabled }
    }

    suspend fun setVibrate(enabled: Boolean) {
        dataStore.edit { it[KEY_VIBRATE] = enabled }
    }

    suspend fun setDefaultBoardSize(size: Int) {
        dataStore.edit { it[KEY_BOARD_SIZE] = size }
    }

    suspend fun setDefaultLevel(level: AiLevel) {
        dataStore.edit { it[KEY_LEVEL] = level.name }
    }

    suspend fun setDefaultMode(mode: GameMode) {
        dataStore.edit { it[KEY_MODE] = mode.name }
    }

    suspend fun setDefaultHumanColor(color: Stone) {
        dataStore.edit { it[KEY_HUMAN_COLOR] = color.name }
    }

    suspend fun setHasSeenTutorial(seen: Boolean) {
        dataStore.edit { it[KEY_SEEN_TUTORIAL] = seen }
    }

    companion object {
        @Volatile
        private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }

        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_COORDINATES = booleanPreferencesKey("show_coordinates")
        private val KEY_VIBRATE = booleanPreferencesKey("vibrate")
        private val KEY_BOARD_SIZE = intPreferencesKey("default_board_size")
        private val KEY_LEVEL = stringPreferencesKey("default_level")
        private val KEY_MODE = stringPreferencesKey("default_mode")
        private val KEY_HUMAN_COLOR = stringPreferencesKey("default_human_color")
        private val KEY_SEEN_TUTORIAL = booleanPreferencesKey("has_seen_tutorial")
    }
}
