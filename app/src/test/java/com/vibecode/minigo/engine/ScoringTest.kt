package com.vibecode.minigo.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {

    @Test
    fun `area scoring counts stones and enclosed territory`() {
        val game = GameState(
            board = Board.fromAscii(
                listOf(
                    "XXXXX",
                    "X...X",
                    "X...X",
                    "X...X",
                    "XXXXX",
                ),
            ),
        )
        val result = Scoring.result(game)
        assertEquals(16, result.blackStones)
        assertEquals(9, result.blackTerritory)
        assertEquals(0, result.whiteStones)
        assertEquals(0, result.whiteTerritory)
        assertEquals(25.0, result.blackScore, 0.001)
        assertEquals(6.5, result.whiteScore, 0.001)
        assertEquals(Stone.BLACK, result.winner)
        assertEquals(18.5, result.margin, 0.001)
    }

    @Test
    fun `dead stones are removed before scoring`() {
        val game = GameState(
            board = Board.fromAscii(
                listOf(
                    "XXXXX",
                    "X...X",
                    "X.O.X",
                    "X...X",
                    "XXXXX",
                ),
            ),
        )
        val before = Scoring.result(game)
        assertEquals(16, before.blackStones)
        assertEquals(1, before.whiteStones)

        val withDead = game.copy(deadStones = listOf(Point(2, 2)))
        val after = Scoring.result(withDead)
        assertEquals(16, after.blackStones)
        assertEquals(0, after.whiteStones)
        assertEquals(9, after.blackTerritory)
        assertEquals(Stone.BLACK, after.winner)
    }

    @Test
    fun `neutral area is not counted`() {
        val game = GameState(
            board = Board.fromAscii(
                listOf(
                    "X...O",
                    ".....",
                    ".....",
                    ".....",
                    "X...O",
                ),
            ),
        )
        val result = Scoring.result(game)
        assertEquals(2, result.blackStones)
        assertEquals(2, result.whiteStones)
        assertEquals(0, result.blackTerritory)
        assertEquals(0, result.whiteTerritory)
        assertEquals(21, result.neutral)
        assertEquals(Stone.WHITE, result.winner)
    }

    @Test
    fun `empty board awards komi to white`() {
        val game = GameState(board = Board.empty(9))
        val result = Scoring.result(game)
        assertEquals(Stone.WHITE, result.winner)
        assertEquals(0.0, result.blackScore, 0.001)
        assertEquals(6.5, result.whiteScore, 0.001)
    }
}


