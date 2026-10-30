package com.vibecode.minigo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vibecode.minigo.data.GameStore
import com.vibecode.minigo.data.SavedGame
import com.vibecode.minigo.engine.GamePhase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val savedGame: SavedGame? = null,
)

class HomeViewModel(
    private val repository: GameStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val saved = repository.load()?.takeIf { it.state.phase != GamePhase.FINISHED }
            _uiState.value = HomeUiState(savedGame = saved)
        }
    }

    fun discardSavedGame() {
        viewModelScope.launch {
            repository.clear()
            _uiState.value = HomeUiState(savedGame = null)
        }
    }

    companion object {
        fun factory(repository: GameStore): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(repository) }
        }
    }
}
