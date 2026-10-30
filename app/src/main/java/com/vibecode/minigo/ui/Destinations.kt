package com.vibecode.minigo.ui

import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.data.GameConfig
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.engine.Stone
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination

@Serializable
data object SetupDestination

@Serializable
data object ResumeDestination

@Serializable
data object TutorialDestination

@Serializable
data object SettingsDestination

@Serializable
data object AboutDestination

@Serializable
data class GameDestination(
    val boardSize: Int,
    val mode: String,
    val level: String,
    val humanColor: String,
    val sessionId: String,
) {
    fun toConfig(): GameConfig = GameConfig(
        boardSize = boardSize,
        mode = runCatching { GameMode.valueOf(mode) }.getOrDefault(GameMode.AI),
        level = runCatching { AiLevel.valueOf(level) }.getOrDefault(AiLevel.EASY),
        humanColor = runCatching { Stone.valueOf(humanColor) }.getOrDefault(Stone.BLACK),
        sessionId = sessionId,
    )

    companion object {
        fun from(config: GameConfig): GameDestination = GameDestination(
            boardSize = config.boardSize,
            mode = config.mode.name,
            level = config.level.name,
            humanColor = config.humanColor.name,
            sessionId = config.sessionId,
        )
    }
}
