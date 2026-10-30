package com.vibecode.minigo.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vibecode.minigo.R
import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.data.AppSettings
import com.vibecode.minigo.data.GameConfig
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.data.withNewSession
import com.vibecode.minigo.engine.Stone
import com.vibecode.minigo.ui.common.BackTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameSetupScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onStart: (GameConfig) -> Unit,
) {
    var boardSize by rememberSaveable { mutableIntStateOf(settings.defaultBoardSize) }
    var mode by rememberSaveable { mutableStateOf(settings.defaultMode) }
    var level by rememberSaveable { mutableStateOf(settings.defaultLevel) }
    var color by rememberSaveable { mutableStateOf(settings.defaultHumanColor) }

    Scaffold(
        topBar = { BackTopBar(title = stringResource(R.string.setup_title), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(stringResource(R.string.setup_board))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = boardSize == 9,
                    onClick = { boardSize = 9 },
                    label = { Text(stringResource(R.string.size_9)) },
                )
                FilterChip(
                    selected = boardSize == 13,
                    onClick = { boardSize = 13 },
                    label = { Text(stringResource(R.string.size_13)) },
                )
            }

            SectionTitle(stringResource(R.string.setup_mode))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == GameMode.AI,
                    onClick = { mode = GameMode.AI },
                    label = { Text(stringResource(R.string.mode_ai)) },
                )
                FilterChip(
                    selected = mode == GameMode.LOCAL,
                    onClick = { mode = GameMode.LOCAL },
                    label = { Text(stringResource(R.string.mode_local)) },
                )
            }

            if (mode == GameMode.AI) {
                SectionTitle(stringResource(R.string.setup_level))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiLevel.entries.forEach { item ->
                        FilterChip(
                            selected = level == item,
                            onClick = { level = item },
                            label = { Text(levelLabel(item)) },
                        )
                    }
                }

                SectionTitle(stringResource(R.string.setup_color))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = color == Stone.BLACK,
                        onClick = { color = Stone.BLACK },
                        label = { Text(stringResource(R.string.color_black)) },
                    )
                    FilterChip(
                        selected = color == Stone.WHITE,
                        onClick = { color = Stone.WHITE },
                        label = { Text(stringResource(R.string.color_white)) },
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.setup_rules_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.setup_rules_body),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            Button(
                onClick = {
                    onStart(
                        GameConfig(
                            boardSize = boardSize,
                            mode = mode,
                            level = level,
                            humanColor = if (mode == GameMode.AI) color else Stone.BLACK,
                        ).withNewSession(),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_start))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun levelLabel(level: AiLevel): String = when (level) {
    AiLevel.BEGINNER -> stringResource(R.string.level_beginner)
    AiLevel.EASY -> stringResource(R.string.level_easy)
    AiLevel.MEDIUM -> stringResource(R.string.level_medium)
}
