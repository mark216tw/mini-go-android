package com.vibecode.minigo.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.ai.GoAi
import com.vibecode.minigo.data.GameConfig
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.data.GameStore
import com.vibecode.minigo.data.SavedGame
import com.vibecode.minigo.engine.Board
import com.vibecode.minigo.engine.GameEngine
import com.vibecode.minigo.engine.GamePhase
import com.vibecode.minigo.engine.GameState
import com.vibecode.minigo.engine.IllegalReason
import com.vibecode.minigo.engine.MoveOutcome
import com.vibecode.minigo.engine.Point
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GameUiState(
    val loading: Boolean = true,
    val config: GameConfig = GameConfig(),
    val game: GameState? = null,
    val thinking: Boolean = false,
    val hint: Point? = null,
    val hintLoading: Boolean = false,
)

sealed interface GameEvent {
    data class Illegal(val reason: IllegalReason) : GameEvent

    data object HintNone : GameEvent

    data object UndoUnavailable : GameEvent

    data object SaveFailed : GameEvent

    data object AiPassed : GameEvent

    data object Ended : GameEvent
}

class GameViewModel(
    private val repository: GameStore,
    initialConfig: GameConfig?,
    private val resume: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    private var currentConfig: GameConfig = initialConfig ?: GameConfig()
    private var aiJob: Job? = null
    private var hintJob: Job? = null
    private var saveJob: Job? = null
    private var saveErrorReported = false

    init {
        viewModelScope.launch {
            val saved = repository.load()
            val restored = when {
                saved == null -> null
                resume -> saved
                saved.config == initialConfig -> saved
                else -> null
            }
            if (restored != null) {
                currentConfig = restored.config
                _uiState.value = GameUiState(
                    loading = false,
                    config = restored.config,
                    game = restored.state,
                )
                maybeStartAi()
            } else {
                startNewGame(currentConfig)
            }
        }
    }

    fun onBoardTap(point: Point) {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (game.phase == GamePhase.SCORING) {
            toggleDead(point)
            return
        }
        if (!canHumanAct(snapshot)) return

        when (val outcome = GameEngine.play(game, point)) {
            is MoveOutcome.Success -> {
                cancelHint()
                _uiState.update { it.copy(game = outcome.state, hint = null) }
                persist()
                maybeStartAi()
            }

            is MoveOutcome.Illegal -> _events.tryEmit(GameEvent.Illegal(outcome.reason))
        }
    }

    fun pass() {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (!canHumanAct(snapshot)) return
        cancelHint()
        _uiState.update { it.copy(game = GameEngine.pass(game), hint = null) }
        persist()
        maybeStartAi()
    }

    fun resign() {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (game.phase != GamePhase.PLAYING) return
        cancelHint()
        aiJob?.cancel()
        aiJob = null
        val by = if (snapshot.config.mode == GameMode.AI) snapshot.config.humanColor else game.toPlay
        _uiState.update {
            it.copy(game = GameEngine.resign(game, by), thinking = false, hint = null)
        }
        persist(immediate = true)
    }

    fun endGame() {
        val snapshot = _uiState.value
        if (snapshot.loading) return
        val game = snapshot.game ?: return
        if (game.phase != GamePhase.PLAYING && game.phase != GamePhase.SCORING) return

        cancelHint()
        aiJob?.cancel()
        aiJob = null
        val pendingSave = saveJob
        saveJob = null
        pendingSave?.cancel()

        _uiState.update { it.copy(loading = true, thinking = false, hint = null) }

        viewModelScope.launch {
            pendingSave?.join()
            runCatching { repository.clear() }
            saveErrorReported = false
            _events.tryEmit(GameEvent.Ended)
        }
    }

    fun saveAndExit() {
        val snapshot = _uiState.value
        if (snapshot.loading) return
        val game = snapshot.game
        if (game == null) {
            _events.tryEmit(GameEvent.Ended)
            return
        }

        cancelHint()
        aiJob?.cancel()
        aiJob = null
        val pendingSave = saveJob
        saveJob = null
        pendingSave?.cancel()

        _uiState.update { it.copy(loading = true, thinking = false, hint = null) }

        viewModelScope.launch {
            pendingSave?.join()
            runCatching {
                repository.save(SavedGame(config = snapshot.config, state = game))
            }
            _events.tryEmit(GameEvent.Ended)
        }
    }

    fun undo() {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (game.phase != GamePhase.PLAYING || game.history.isEmpty()) {
            _events.tryEmit(GameEvent.UndoUnavailable)
            return
        }

        aiJob?.cancel()
        aiJob = null
        cancelHint()

        val history = game.history.toMutableList()
        var restored = history.removeAt(history.lastIndex).toState(history)
        if (snapshot.config.mode == GameMode.AI) {
            var guard = 0
            while (
                restored.toPlay != snapshot.config.humanColor &&
                history.isNotEmpty() &&
                guard < 4
            ) {
                restored = history.removeAt(history.lastIndex).toState(history)
                guard++
            }
            if (restored.toPlay != snapshot.config.humanColor) {
                _uiState.update { it.copy(thinking = false) }
                _events.tryEmit(GameEvent.UndoUnavailable)
                return
            }
        }

        _uiState.update { it.copy(game = restored, thinking = false, hint = null) }
        persist()
    }

    fun requestHint() {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (!canHumanAct(snapshot) || snapshot.hintLoading) return
        _uiState.update { it.copy(hint = null, hintLoading = true) }
        hintJob = viewModelScope.launch {
            val point = try {
                withContext(Dispatchers.Default) {
                    val context = currentCoroutineContext()
                    GoAi.chooseMove(game, AiLevel.MEDIUM) { !context.isActive }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                null
            }

            if (_uiState.value.game?.moveNumber == game.moveNumber) {
                _uiState.update { it.copy(hint = point, hintLoading = false) }
                if (point == null) _events.tryEmit(GameEvent.HintNone)
            } else {
                _uiState.update { it.copy(hintLoading = false) }
            }
        }
    }

    fun toggleDead(point: Point) {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (game.phase != GamePhase.SCORING) return
        _uiState.update { it.copy(game = GameEngine.toggleDeadGroup(it.game ?: game, point)) }
        persist()
    }

    fun clearDeadMarks() {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (game.phase != GamePhase.SCORING || game.deadStones.isEmpty()) return
        _uiState.update { state ->
            state.copy(game = (state.game ?: game).copy(deadStones = emptyList()))
        }
        persist()
    }

    fun confirmScoring() {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (game.phase != GamePhase.SCORING) return
        cancelHint()
        aiJob?.cancel()
        aiJob = null
        _uiState.update {
            it.copy(game = GameEngine.confirmScoring(game), thinking = false, hint = null)
        }
        persist(immediate = true)
    }

    fun cancelScoring() {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (game.phase != GamePhase.SCORING) return
        cancelHint()
        _uiState.update { it.copy(game = GameEngine.cancelScoring(game), hint = null) }
        persist()
        maybeStartAi()
    }

    fun newGame() {
        startNewGame(_uiState.value.config)
    }

    private fun startNewGame(config: GameConfig) {
        aiJob?.cancel()
        aiJob = null
        cancelHint()
        saveErrorReported = false
        currentConfig = config
        val state = GameState(board = Board.empty(config.boardSize))
        _uiState.value = GameUiState(
            loading = false,
            config = config,
            game = state,
            thinking = false,
        )
        persist()
        maybeStartAi()
    }

    private fun canHumanAct(snapshot: GameUiState): Boolean {
        val game = snapshot.game ?: return false
        if (game.phase != GamePhase.PLAYING || snapshot.thinking) return false
        return when (snapshot.config.mode) {
            GameMode.LOCAL -> true
            GameMode.AI -> game.toPlay == snapshot.config.humanColor
        }
    }

    private fun maybeStartAi() {
        val snapshot = _uiState.value
        val game = snapshot.game ?: return
        if (snapshot.loading) return
        if (snapshot.config.mode != GameMode.AI) return
        if (game.phase != GamePhase.PLAYING) return
        if (game.toPlay == snapshot.config.humanColor) return
        if (aiJob?.isActive == true) return

        aiJob = viewModelScope.launch {
            delay(320)
            val before = _uiState.value
            val target = before.game ?: return@launch
            if (before.config.mode != GameMode.AI) return@launch
            if (target.phase != GamePhase.PLAYING) return@launch
            if (target.toPlay == before.config.humanColor) return@launch

            _uiState.update { it.copy(thinking = true) }

            val move = try {
                withContext(Dispatchers.Default) {
                    val context = currentCoroutineContext()
                    GoAi.chooseMove(target, before.config.level) { !context.isActive }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                null
            }

            val current = _uiState.value
            val currentGame = current.game ?: return@launch
            if (
                currentGame.phase != GamePhase.PLAYING ||
                currentGame.moveNumber != target.moveNumber
            ) {
                _uiState.update { it.copy(thinking = false) }
                return@launch
            }

            val nextState = if (move != null) {
                when (val outcome = GameEngine.play(currentGame, move)) {
                    is MoveOutcome.Success -> outcome.state
                    is MoveOutcome.Illegal -> {
                        _events.tryEmit(GameEvent.AiPassed)
                        GameEngine.pass(currentGame)
                    }
                }
            } else {
                _events.tryEmit(GameEvent.AiPassed)
                GameEngine.pass(currentGame)
            }

            _uiState.update {
                it.copy(game = nextState, thinking = false, hint = null)
            }
            persist()
            maybeStartAi()
        }
    }

    private fun persist(immediate: Boolean = false) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            if (!immediate) delay(80)
            val snapshot = _uiState.value
            val game = snapshot.game ?: return@launch
            try {
                repository.save(SavedGame(config = snapshot.config, state = game))
                saveErrorReported = false
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                if (!saveErrorReported) {
                    saveErrorReported = true
                    _events.tryEmit(GameEvent.SaveFailed)
                }
            }
        }
    }

    private fun cancelHint() {
        hintJob?.cancel()
        hintJob = null
        if (_uiState.value.hintLoading) {
            _uiState.update { it.copy(hintLoading = false) }
        }
    }

    companion object {
        fun factory(
            repository: GameStore,
            config: GameConfig?,
            resume: Boolean,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { GameViewModel(repository, config, resume) }
        }
    }
}
