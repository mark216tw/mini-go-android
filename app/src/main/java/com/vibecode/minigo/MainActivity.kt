package com.vibecode.minigo

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vibecode.minigo.data.AppSettings
import com.vibecode.minigo.data.SettingsRepository
import com.vibecode.minigo.data.ThemeMode
import com.vibecode.minigo.ui.MiniGoApp
import com.vibecode.minigo.ui.theme.MiniGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current.applicationContext
            val repository = remember { SettingsRepository.get(context) }
            val settings by repository.settings.collectAsStateWithLifecycle(
                initialValue = AppSettings(),
            )
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val view = LocalView.current
            SideEffect {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            MiniGoTheme(
                darkTheme = darkTheme,
                dynamicColor = settings.dynamicColor,
            ) {
                MiniGoApp(
                    settings = settings,
                    settingsRepository = repository,
                )
            }
        }
    }
}
