package com.vibecode.minigo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vibecode.minigo.R
import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.data.GameConfig
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.data.GameStore
import com.vibecode.minigo.ui.common.ActionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: GameStore,
    showTutorialHint: Boolean,
    onDismissTutorialHint: () -> Unit,
    onQuickStart: () -> Unit,
    onPlay: () -> Unit,
    onResume: () -> Unit,
    onTutorial: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showQuickStartDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
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
                                text = { Text(stringResource(R.string.settings_title)) },
                                onClick = {
                                    showMoreMenu = false
                                    onSettings()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.about_title)) },
                                onClick = {
                                    showMoreMenu = false
                                    onAbout()
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (showTutorialHint) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.home_tutorial_prompt_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = stringResource(R.string.home_tutorial_prompt_body),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = onDismissTutorialHint) {
                                Text(stringResource(R.string.home_tutorial_prompt_dismiss))
                            }
                            Button(onClick = onTutorial) {
                                Text(stringResource(R.string.home_tutorial_prompt_open))
                            }
                        }
                    }
                }
            }

            val saved = uiState.savedGame
            if (saved != null) {
                val modeLabel = resumeModeLabel(saved.config)
                Column {
                    ActionCard(
                        title = stringResource(R.string.home_resume),
                        description = stringResource(
                            R.string.home_resume_summary_full,
                            saved.config.boardSize,
                            modeLabel,
                            saved.state.moveNumber,
                        ),
                        onClick = onResume,
                        emphasized = true,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showDiscardDialog = true }) {
                            Text(stringResource(R.string.home_discard))
                        }
                    }
                }
            }

            ActionCard(
                title = stringResource(R.string.home_quick_start),
                description = stringResource(R.string.home_quick_start_desc),
                onClick = {
                    if (saved != null) {
                        showQuickStartDialog = true
                    } else {
                        onQuickStart()
                    }
                },
            )
            ActionCard(
                title = stringResource(R.string.home_play),
                description = stringResource(R.string.home_play_desc),
                onClick = onPlay,
            )
            ActionCard(
                title = stringResource(R.string.home_tutorial),
                description = stringResource(R.string.home_tutorial_desc),
                onClick = onTutorial,
            )
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.discard_title)) },
            text = { Text(stringResource(R.string.discard_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        viewModel.discardSavedGame()
                    },
                ) { Text(stringResource(R.string.discard_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showQuickStartDialog) {
        AlertDialog(
            onDismissRequest = { showQuickStartDialog = false },
            title = { Text(stringResource(R.string.home_quick_start_confirm_title)) },
            text = { Text(stringResource(R.string.home_quick_start_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQuickStartDialog = false
                        onQuickStart()
                    },
                ) { Text(stringResource(R.string.home_quick_start_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showQuickStartDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun resumeModeLabel(config: GameConfig): String = when (config.mode) {
    GameMode.AI -> stringResource(
        R.string.mode_label_ai,
        levelLabel(config.level),
    )

    GameMode.LOCAL -> stringResource(R.string.mode_label_local)
}

@Composable
private fun levelLabel(level: AiLevel): String = when (level) {
    AiLevel.BEGINNER -> stringResource(R.string.level_beginner)
    AiLevel.EASY -> stringResource(R.string.level_easy)
    AiLevel.MEDIUM -> stringResource(R.string.level_medium)
}
