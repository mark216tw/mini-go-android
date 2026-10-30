package com.vibecode.minigo.ui

import com.vibecode.minigo.ai.AiLevel
import com.vibecode.minigo.data.GameConfig
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.engine.Stone
import org.junit.Assert.assertEquals
import org.junit.Test

class DestinationsTest {

    @Test
    fun `game destination round trips config`() {
        val config = GameConfig(
            boardSize = 13,
            mode = GameMode.LOCAL,
            level = AiLevel.BEGINNER,
            humanColor = Stone.WHITE,
            sessionId = "session-1",
        )
        assertEquals(config, GameDestination.from(config).toConfig())
    }
}
