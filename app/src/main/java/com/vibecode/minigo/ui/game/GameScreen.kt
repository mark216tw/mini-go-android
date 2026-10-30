package com.vibecode.minigo.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibecode.minigo.R
import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.engine.EndReason
import com.vibecode.minigo.engine.GamePhase
import com.vibecode.minigo.engine.GameState
import com.vibecode.minigo.engine.IllegalReason
import com.vibecode.minigo.engine.Point
import com.vibecode.minigo.engine.ScoreResult
import com.vibecode.minigo.engine.Scoring
import com.vibecode.minigo.engine.Stone
import com.vibecode.minigo.ui.common.BackTopBar
import com.vibecode.minigo.ui.common.toGoCoordinate
import java.util.Locale
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    events: Flow<GameEvent>,
    showCoordinates: Boolean,
    vibrate: Boolean,
    onTap: (Point) -> Unit,
    onUndo: () -> Unit,
    onHint: () -> Unit,
    onPass: () -> Unit,
    onResign: () -> Unit,
    onConfirmScoring: () -> Unit,
    onCancelScoring: () -> Unit,
    onClearDeadMarks: () -> Unit,
    onNewGame: () -> Unit,
    onRestart: () -> Unit,
    onOpenTutorial: () -> Unit,
    onEndGame: () -> Unit,
    onSaveAndExit: () -> Unit,
    onExit: () -> Unit,
) {
    val game = uiState.game
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    var showPassDialog by remember { mutableStateOf(false) }
    var showResignDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showEndGameDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showScoringHelp by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var resultDismissed by remember { mutableStateOf(false) }

    val illegalOccupied = stringResource(R.string.illegal_occupied)
    val illegalSuicide = stringResource(R.string.illegal_suicide)
    val illegalKo = stringResource(R.string.illegal_ko)
    val illegalNotPlaying = stringResource(R.string.illegal_not_playing)
    val hintNone = stringResource(R.string.hint_none)
    val undoUnavailable = stringResource(R.string.undo_unavailable)
    val saveFailed = stringResource(R.string.save_failed)
    val aiPassed = stringResource(R.string.ai_passed)

    LaunchedEffect(game?.phase, game?.moveNumber) {
        if (game?.phase != GamePhase.FINISHED) {
            resultDismissed = false
        }
    }

    LaunchedEffect(events) {
        events.collect { event ->
            val message = when (event) {
                GameEvent.Ended -> {
                    onExit()
                    return@collect
                }

                is GameEvent.Illegal -> when (event.reason) {
                    IllegalReason.OCCUPIED -> illegalOccupied
                    IllegalReason.SUICIDE -> illegalSuicide
                    IllegalReason.KO -> illegalKo
                    IllegalReason.NOT_PLAYING -> illegalNotPlaying
                }

                GameEvent.HintNone -> hintNone
                GameEvent.UndoUnavailable -> undoUnavailable
                GameEvent.SaveFailed -> saveFailed
                GameEvent.AiPassed -> aiPassed
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    var lastHandledMove by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(game?.moveNumber, vibrate) {
        val move = game?.moveNumber ?: return@LaunchedEffect
        val previous = lastHandledMove
        if (previous == null) {
            lastHandledMove = move
            return@LaunchedEffect
        }
        if (vibrate && move > previous) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        lastHandledMove = move
    }

    val requestExit = {
        when {
            uiState.loading -> Unit
            game == null -> onExit()
            game.phase == GamePhase.FINISHED -> onSaveAndExit()
            else -> showLeaveDialog = true
        }
    }
    BackHandler {
        when {
            uiState.loading -> Unit
            showScoringHelp -> showScoringHelp = false
            showRestartDialog -> showRestartDialog = false
            showEndGameDialog -> showEndGameDialog = false
            showMoreMenu -> showMoreMenu = false
            showPassDialog -> showPassDialog = false
            showResignDialog -> showResignDialog = false
            showLeaveDialog -> showLeaveDialog = false
            game == null -> onExit()
            game.phase == GamePhase.FINISHED -> onSaveAndExit()
            else -> showLeaveDialog = true
        }
    }

    val modeLabel = when (uiState.config.mode) {
        GameMode.AI -> stringResource(
            R.string.mode_label_ai,
            levelLabel(uiState.config.level),
        )

        GameMode.LOCAL -> stringResource(R.string.mode_label_local)
    }

    Scaffold(
        topBar = {
            BackTopBar(
                title = stringResource(
                    R.string.game_title,
                    uiState.config.boardSize,
                    modeLabel,
                ),
                onBack = requestExit,
                actions = {
                    if (!uiState.loading && game != null && game.phase != GamePhase.FINISHED) {
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.action_more),
                                )
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_restart)) },
                                    onClick = {
                                        showMoreMenu = false
                                        showRestartDialog = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_rules)) },
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenTutorial()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_end_game)) },
                                    onClick = {
                                        showMoreMenu = false
                                        showEndGameDialog = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (uiState.loading || game == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val humanTurn = when (uiState.config.mode) {
            GameMode.LOCAL -> true
            GameMode.AI -> game.toPlay == uiState.config.humanColor
        }
        val boardEnabled = game.phase == GamePhase.SCORING ||
            (game.phase == GamePhase.PLAYING && humanTurn && !uiState.thinking)
        val deadStoneSet = remember(game.deadStones) { game.deadStones.toSet() }
        val scorePreview = remember(game.board, game.deadStones, game.phase) {
            if (game.phase == GamePhase.SCORING) Scoring.result(game) else null
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val boardSize = minOf(maxWidth - 32.dp, maxHeight * 0.55f)
                .coerceAtLeast(180.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GameStatusCard(uiState = uiState, game = game)

                if (game.phase == GamePhase.SCORING) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.scoring_banner),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = stringResource(
                                        R.string.scoring_dead_count,
                                        game.deadStones.size,
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            TextButton(onClick = { showScoringHelp = true }) {
                                Text(stringResource(R.string.scoring_help))
                            }
                        }
                    }
                }

                GoBoard(
                    board = game.board,
                    lastMove = game.lastMove,
                    hint = uiState.hint,
                    deadStones = deadStoneSet,
                    showCoordinates = showCoordinates,
                    enabled = boardEnabled,
                    onTap = onTap,
                    modifier = Modifier
                        .size(boardSize)
                        .align(Alignment.CenterHorizontally),
                )

                if (game.phase == GamePhase.SCORING) {
                    scorePreview?.let { ScorePreviewCard(it) }
                }

                when (game.phase) {
                    GamePhase.PLAYING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = onUndo,
                                enabled = game.history.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) { Text(stringResource(R.string.action_undo)) }

                            OutlinedButton(
                                onClick = onHint,
                                enabled = humanTurn && !uiState.thinking && !uiState.hintLoading,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    stringResource(
                                        if (uiState.hintLoading) {
                                            R.string.hint_calculating
                                        } else {
                                            R.string.action_hint
                                        },
                                    ),
                                )
                            }

                            OutlinedButton(
                                onClick = { showPassDialog = true },
                                enabled = humanTurn && !uiState.thinking,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) { Text(stringResource(R.string.action_pass)) }

                            OutlinedButton(
                                onClick = { showResignDialog = true },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) { Text(stringResource(R.string.action_resign)) }
                        }
                    }

                    GamePhase.SCORING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = onClearDeadMarks,
                                enabled = game.deadStones.isNotEmpty(),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) { Text(stringResource(R.string.scoring_clear_marks)) }

                            OutlinedButton(
                                onClick = onCancelScoring,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) { Text(stringResource(R.string.scoring_cancel)) }

                            Button(
                                onClick = onConfirmScoring,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            ) { Text(stringResource(R.string.scoring_confirm)) }
                        }
                    }

                    GamePhase.FINISHED -> {
                        OutlinedButton(
                            onClick = { resultDismissed = false },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        ) { Text(stringResource(R.string.action_view_result)) }
                    }
                }
            }
        }

        if (game.phase == GamePhase.FINISHED && !uiState.loading && !resultDismissed) {
            GameResultDialog(
                game = game,
                onViewBoard = { resultDismissed = true },
                onNewGame = onNewGame,
                onExit = onSaveAndExit,
            )
        }
    }

    if (showPassDialog) {
        AlertDialog(
            onDismissRequest = { showPassDialog = false },
            title = { Text(stringResource(R.string.pass_title)) },
            text = { Text(stringResource(R.string.pass_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPassDialog = false
                        onPass()
                    },
                ) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showPassDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showResignDialog) {
        AlertDialog(
            onDismissRequest = { showResignDialog = false },
            title = { Text(stringResource(R.string.resign_title)) },
            text = { Text(stringResource(R.string.resign_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResignDialog = false
                        onResign()
                    },
                ) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showResignDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showScoringHelp) {
        AlertDialog(
            onDismissRequest = { showScoringHelp = false },
            title = { Text(stringResource(R.string.scoring_help_title)) },
            text = { Text(stringResource(R.string.scoring_help_text)) },
            confirmButton = {
                TextButton(onClick = { showScoringHelp = false }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(stringResource(R.string.restart_title)) },
            text = { Text(stringResource(R.string.restart_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        onRestart()
                    },
                ) { Text(stringResource(R.string.restart_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showEndGameDialog) {
        AlertDialog(
            onDismissRequest = { showEndGameDialog = false },
            title = { Text(stringResource(R.string.end_game_title)) },
            text = { Text(stringResource(R.string.end_game_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndGameDialog = false
                        onEndGame()
                    },
                ) { Text(stringResource(R.string.end_game_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndGameDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text(stringResource(R.string.leave_title)) },
            text = { Text(stringResource(R.string.leave_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        onSaveAndExit()
                    },
                ) { Text(stringResource(R.string.leave_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun GameStatusCard(uiState: GameUiState, game: GameState) {
    val status = when {
        game.phase == GamePhase.FINISHED -> stringResource(R.string.game_status_finished)
        game.phase == GamePhase.SCORING -> stringResource(R.string.game_status_scoring)
        uiState.thinking -> stringResource(R.string.game_status_thinking)
        game.toPlay == Stone.BLACK -> stringResource(R.string.game_status_black)
        else -> stringResource(R.string.game_status_white)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = game.lastMove?.let { last ->
                        stringResource(
                            R.string.game_move_with_last,
                            game.moveNumber,
                            last.toGoCoordinate(game.board.size),
                        )
                    } ?: stringResource(R.string.game_move_number, game.moveNumber),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(
                        R.string.game_captures,
                        game.blackCaptures,
                        game.whiteCaptures,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(R.string.game_komi),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            uiState.hint?.let { hint ->
                Text(
                    text = stringResource(
                        R.string.hint_suggested,
                        hint.toGoCoordinate(game.board.size),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ScorePreviewCard(result: ScoreResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.scoring_preview_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(
                    R.string.score_breakdown_black,
                    result.blackStones,
                    result.blackTerritory,
                ),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = stringResource(
                    R.string.score_breakdown_white,
                    result.whiteStones,
                    result.whiteTerritory,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            if (result.neutral > 0) {
                Text(
                    text = stringResource(R.string.score_neutral, result.neutral),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = stringResource(
                    R.string.result_total,
                    formatScore(result.blackScore),
                    formatScore(result.whiteScore),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun GameResultDialog(
    game: GameState,
    onViewBoard: () -> Unit,
    onNewGame: () -> Unit,
    onExit: () -> Unit,
) {
    val result = if (game.endReason == EndReason.TWO_PASSES) Scoring.result(game) else null
    val heading = when {
        result != null -> when (result.winner) {
            Stone.BLACK -> stringResource(R.string.result_black_win)
            Stone.WHITE -> stringResource(R.string.result_white_win)
            else -> stringResource(R.string.result_draw)
        }

        game.endReason == EndReason.BLACK_RESIGNED -> stringResource(
            R.string.result_resign,
            stringResource(R.string.stone_white),
        )

        game.endReason == EndReason.WHITE_RESIGNED -> stringResource(
            R.string.result_resign,
            stringResource(R.string.stone_black),
        )

        else -> stringResource(R.string.result_draw)
    }

    AlertDialog(
        onDismissRequest = onViewBoard,
        title = { Text(stringResource(R.string.game_over)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = heading,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (result != null) {
                    Text(
                        text = stringResource(
                            R.string.result_black_area,
                            result.blackStones + result.blackTerritory,
                        ),
                    )
                    Text(
                        text = stringResource(
                            R.string.result_white_area,
                            result.whiteStones + result.whiteTerritory,
                        ),
                    )
                    Text(
                        text = stringResource(
                            R.string.result_komi_row,
                            formatScore(result.komi),
                        ),
                    )
                    Text(
                        text = stringResource(
                            R.string.result_total,
                            formatScore(result.blackScore),
                            formatScore(result.whiteScore),
                        ),
                    )
                    Text(
                        text = stringResource(
                            R.string.result_margin,
                            formatScore(result.margin),
                        ),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onNewGame) {
                Text(stringResource(R.string.action_new_game))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onViewBoard) {
                    Text(stringResource(R.string.action_view_board))
                }
                TextButton(onClick = onExit) {
                    Text(stringResource(R.string.action_home))
                }
            }
        },
    )
}

@Composable
private fun levelLabel(level: AiLevel): String = when (level) {
    AiLevel.BEGINNER -> stringResource(R.string.level_beginner)
    AiLevel.EASY -> stringResource(R.string.level_easy)
    AiLevel.MEDIUM -> stringResource(R.string.level_medium)
}

private fun formatScore(value: Double): String =
    if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.TAIWAN, "%.1f", value)
    }
