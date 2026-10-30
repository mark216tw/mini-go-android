package com.vibecode.minigo.engine

import com.vibecode.minigo.data.GameConfig
import com.vibecode.minigo.data.GameMode
import com.vibecode.minigo.data.SavedGame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class PersistenceTest {

    @Test
    fun `saved game survives json round trip`() {
        val config = GameConfig(
            boardSize = 9,
            mode = GameMode.AI,
            humanColor = Stone.BLACK,
        )
        var game = GameState(board = Board.empty(9))
        game = GameEngine.play(game, Point(4, 4)).let { (it as MoveOutcome.Success).state }
        game = GameEngine.play(game, Point(3, 3)).let { (it as MoveOutcome.Success).state }
        game = GameEngine.pass(game)
        game = GameEngine.pass(game)

        val saved = SavedGame(config = config, state = game)
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val encoded = json.encodeToString(saved)
        val decoded = json.decodeFromString<SavedGame>(encoded)

        assertEquals(saved, decoded)
        assertEquals(GamePhase.SCORING, decoded.state.phase)
        assertEquals(4, decoded.state.history.size)
    }
}
