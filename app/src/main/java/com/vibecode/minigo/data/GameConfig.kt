package com.vibecode.minigo.data

import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.engine.Stone
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
enum class GameMode {
    AI,
    LOCAL,
}

@Serializable
data class GameConfig(
    val boardSize: Int = 9,
    val mode: GameMode = GameMode.AI,
    val level: AiLevel = AiLevel.EASY,
    val humanColor: Stone = Stone.BLACK,
    val sessionId: String = "",
)

fun GameConfig.withNewSession(): GameConfig = copy(sessionId = UUID.randomUUID().toString())
