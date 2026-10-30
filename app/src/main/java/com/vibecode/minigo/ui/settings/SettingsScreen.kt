package com.vibecode.minigo.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vibecode.minigo.R
import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.data.ThemeMode
import com.vibecode.minigo.engine.Stone
import com.vibecode.minigo.ui.common.BackTopBar

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BackTopBar(title = stringResource(R.string.settings_title), onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
        ) {
            SectionTitle(stringResource(R.string.settings_theme))
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        label = { Text(themeLabel(mode)) },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SwitchRow(
                title = stringResource(R.string.settings_dynamic),
                description = stringResource(R.string.settings_dynamic_desc),
                checked = settings.dynamicColor,
                onCheckedChange = viewModel::setDynamicColor,
            )
            SwitchRow(
                title = stringResource(R.string.settings_coordinates),
                description = "",
                checked = settings.showCoordinates,
                onCheckedChange = viewModel::setShowCoordinates,
            )
            SwitchRow(
                title = stringResource(R.string.settings_vibrate),
                description = "",
                checked = settings.vibrate,
                onCheckedChange = viewModel::setVibrate,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SectionTitle(stringResource(R.string.settings_default_mode))
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = settings.defaultMode == GameMode.AI,
                    onClick = { viewModel.setDefaultMode(GameMode.AI) },
                    label = { Text(stringResource(R.string.mode_ai)) },
                )
                FilterChip(
                    selected = settings.defaultMode == GameMode.LOCAL,
                    onClick = { viewModel.setDefaultMode(GameMode.LOCAL) },
                    label = { Text(stringResource(R.string.mode_local)) },
                )
            }

            SectionTitle(stringResource(R.string.settings_default_board))
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = settings.defaultBoardSize == 9,
                    onClick = { viewModel.setDefaultBoardSize(9) },
                    label = { Text(stringResource(R.string.size_9)) },
                )
                FilterChip(
                    selected = settings.defaultBoardSize == 13,
                    onClick = { viewModel.setDefaultBoardSize(13) },
                    label = { Text(stringResource(R.string.size_13)) },
                )
            }

            SectionTitle(
                text = stringResource(R.string.settings_default_level),
                modifier = Modifier.padding(top = 8.dp),
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AiLevel.entries.forEach { level ->
                    FilterChip(
                        selected = settings.defaultLevel == level,
                        onClick = { viewModel.setDefaultLevel(level) },
                        label = { Text(levelLabel(level)) },
                    )
                }
            }

            SectionTitle(
                text = stringResource(R.string.settings_default_color),
                modifier = Modifier.padding(top = 8.dp),
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = settings.defaultHumanColor == Stone.BLACK,
                    onClick = { viewModel.setDefaultHumanColor(Stone.BLACK) },
                    label = { Text(stringResource(R.string.color_black)) },
                )
                FilterChip(
                    selected = settings.defaultHumanColor == Stone.WHITE,
                    onClick = { viewModel.setDefaultHumanColor(Stone.WHITE) },
                    label = { Text(stringResource(R.string.color_white)) },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (description.isNotEmpty()) {
            { Text(description) }
        } else {
            null
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) },
    )
}

@Composable
private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
    ThemeMode.DARK -> stringResource(R.string.theme_dark)
}

@Composable
private fun levelLabel(level: AiLevel): String = when (level) {
    AiLevel.BEGINNER -> stringResource(R.string.level_beginner)
    AiLevel.EASY -> stringResource(R.string.level_easy)
    AiLevel.MEDIUM -> stringResource(R.string.level_medium)
}
