package com.vibecode.minigo.ai

import com.vibecode.minigo.engine.Board
import com.vibecode.minigo.engine.GameEngine
import com.vibecode.minigo.engine.GameState
import com.vibecode.minigo.engine.MoveOutcome
import com.vibecode.minigo.engine.Point
import com.vibecode.minigo.engine.Stone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GoAiTest {

    private fun state(
        rows: List<String>,
        toPlay: Stone = Stone.BLACK,
    ): GameState = GameState(board = Board.fromAscii(rows), toPlay = toPlay)

    @Test
    fun `all levels only return legal moves`() {
        val game = state(
            listOf(
                ".X.O.....",
                "X.O......",
                ".X.O.....",
                "....O....",
                ".........",
                "...XXX...",
                "...X.X...",
                ".........",
                ".........",
            ),
            toPlay = Stone.WHITE,
        )
        for (level in AiLevel.entries) {
            repeat(20) {
                val move = GoAi.chooseMove(game, level) ?: return@repeat
                assertTrue(
                    "Illegal move $move for $level",
                    GameEngine.play(game, move) is MoveOutcome.Success,
                )
            }
        }
    }

    @Test
    fun `easy level takes an obvious capture`() {
        val game = state(
            listOf(
                ".X.",
                "XO.",
                ".X.",
            ),
        )
        val move = GoAi.chooseMove(game, AiLevel.EASY)
        assertEquals(Point(2, 1), move)
    }

    @Test
    fun `ai passes when no legal moves remain`() {
        val board = Board.fromAscii(
            listOf(
                "XO",
                "OX",
            ),
        )
        val game = GameState(board = board, toPlay = Stone.BLACK)
        val move = GoAi.chooseMove(game, AiLevel.MEDIUM)
        assertTrue(move == null)
    }

    @Test
    fun `ai returns a legal move even with exhausted time budget`() {
        val game = state(
            listOf(
                ".X.O.....",
                "X.O......",
                ".X.O.....",
                "....O....",
                ".........",
                "...XXX...",
                "...X.X...",
                ".........",
                ".........",
            ),
            toPlay = Stone.WHITE,
        )
        val move = GoAi.chooseMove(game, AiLevel.MEDIUM, timeBudgetMillis = 0L)
        assertTrue(move != null)
        assertTrue(GameEngine.play(game, move!!) is MoveOutcome.Success)
    }

    @Test
    fun `ai stops immediately when cancellation is requested`() {
        val game = state(
            listOf(
                ".X.O.....",
                "X.O......",
                ".X.O.....",
                "....O....",
                ".........",
                "...XXX...",
                "...X.X...",
                ".........",
                ".........",
            ),
            toPlay = Stone.WHITE,
        )
        val move = GoAi.chooseMove(game, AiLevel.MEDIUM, shouldStop = { true })
        assertTrue(move == null)
    }
}
