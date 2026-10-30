package com.vibecode.minigo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vibecode.minigo.data.AppSettings
import com.vibecode.minigo.data.GameConfig
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.data.GameRepository
import com.vibecode.minigo.data.GameStore
import com.vibecode.minigo.data.SettingsRepository
import com.vibecode.minigo.data.withNewSession
import com.vibecode.minigo.engine.Stone
import com.vibecode.minigo.ui.about.AboutScreen
import com.vibecode.minigo.ui.game.GameScreen
import com.vibecode.minigo.ui.game.GameViewModel
import com.vibecode.minigo.ui.home.HomeScreen
import com.vibecode.minigo.ui.settings.SettingsScreen
import com.vibecode.minigo.ui.setup.GameSetupScreen
import com.vibecode.minigo.ui.tutorial.TutorialScreen
import kotlinx.coroutines.launch

@Composable
fun MiniGoApp(
    settings: AppSettings,
    settingsRepository: SettingsRepository,
) {
    val context = LocalContext.current
    val gameRepository = remember { GameRepository.get(context) }
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val markTutorialSeen: () -> Unit = {
        scope.launch { settingsRepository.setHasSeenTutorial(true) }
        Unit
    }
    val goHome = {
        navController.popBackStack(HomeDestination, inclusive = false)
        Unit
    }
    val openTutorial = {
        markTutorialSeen()
        navController.navigate(TutorialDestination)
        Unit
    }
    val quickStart = {
        val mode = settings.defaultMode
        val config = GameConfig(
            boardSize = settings.defaultBoardSize,
            mode = mode,
            level = settings.defaultLevel,
            humanColor = if (mode == GameMode.AI) settings.defaultHumanColor else Stone.BLACK,
        ).withNewSession()
        navController.navigate(GameDestination.from(config))
        Unit
    }

    NavHost(navController = navController, startDestination = HomeDestination) {
        composable<HomeDestination> {
            HomeScreen(
                repository = gameRepository,
                showTutorialHint = !settings.hasSeenTutorial,
                onDismissTutorialHint = markTutorialSeen,
                onQuickStart = quickStart,
                onPlay = { navController.navigate(SetupDestination) },
                onResume = { navController.navigate(ResumeDestination) },
                onTutorial = openTutorial,
                onSettings = { navController.navigate(SettingsDestination) },
                onAbout = { navController.navigate(AboutDestination) },
            )
        }

        composable<SetupDestination> {
            GameSetupScreen(
                settings = settings,
                onBack = { navController.popBackStack() },
                onStart = { config -> navController.navigate(GameDestination.from(config)) },
            )
        }

        composable<GameDestination> { entry ->
            val destination = entry.toRoute<GameDestination>()
            GameScreenRoute(
                repository = gameRepository,
                config = destination.toConfig(),
                resume = false,
                settings = settings,
                onOpenTutorial = openTutorial,
                onExit = goHome,
            )
        }

        composable<ResumeDestination> {
            GameScreenRoute(
                repository = gameRepository,
                config = null,
                resume = true,
                settings = settings,
                onOpenTutorial = openTutorial,
                onExit = goHome,
            )
        }

        composable<TutorialDestination> {
            TutorialScreen(onBack = { navController.popBackStack() })
        }

        composable<SettingsDestination> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable<AboutDestination> {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun GameScreenRoute(
    repository: GameStore,
    config: GameConfig?,
    resume: Boolean,
    settings: AppSettings,
    onOpenTutorial: () -> Unit,
    onExit: () -> Unit,
) {
    val viewModel: GameViewModel = viewModel(
        key = if (resume) "resume" else "game-${config?.sessionId}",
        factory = GameViewModel.factory(repository, config, resume),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GameScreen(
        uiState = uiState,
        events = viewModel.events,
        showCoordinates = settings.showCoordinates,
        vibrate = settings.vibrate,
        onTap = viewModel::onBoardTap,
        onUndo = viewModel::undo,
        onHint = viewModel::requestHint,
        onPass = viewModel::pass,
        onResign = viewModel::resign,
        onConfirmScoring = viewModel::confirmScoring,
        onCancelScoring = viewModel::cancelScoring,
        onClearDeadMarks = viewModel::clearDeadMarks,
        onNewGame = viewModel::newGame,
        onRestart = viewModel::newGame,
        onOpenTutorial = onOpenTutorial,
        onEndGame = viewModel::endGame,
        onSaveAndExit = viewModel::saveAndExit,
        onExit = onExit,
    )
}
