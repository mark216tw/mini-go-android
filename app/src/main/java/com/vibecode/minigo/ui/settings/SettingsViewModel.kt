package com.vibecode.minigo.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.data.AppSettings
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.data.SettingsRepository
import com.vibecode.minigo.data.ThemeMode
import com.vibecode.minigo.engine.Stone
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository.get(application)

    val settings = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColor(enabled) }
    }

    fun setShowCoordinates(enabled: Boolean) {
        viewModelScope.launch { repository.setShowCoordinates(enabled) }
    }

    fun setVibrate(enabled: Boolean) {
        viewModelScope.launch { repository.setVibrate(enabled) }
    }

    fun setDefaultBoardSize(size: Int) {
        viewModelScope.launch { repository.setDefaultBoardSize(size) }
    }

    fun setDefaultLevel(level: AiLevel) {
        viewModelScope.launch { repository.setDefaultLevel(level) }
    }

    fun setDefaultMode(mode: GameMode) {
        viewModelScope.launch { repository.setDefaultMode(mode) }
    }

    fun setDefaultHumanColor(color: Stone) {
        viewModelScope.launch { repository.setDefaultHumanColor(color) }
    }
}
