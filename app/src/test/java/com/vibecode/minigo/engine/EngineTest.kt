package com.vibecode.minigo.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {

    private fun state(
        rows: List<String>,
        toPlay: Stone = Stone.BLACK,
    ): GameState = GameState(board = Board.fromAscii(rows), toPlay = toPlay)

    @Test
    fun `occupied point is rejected`() {
        val game = state(listOf("X..", "...", "..."))
        val outcome = GameEngine.play(game, Point(0, 0))
        assertTrue(outcome is MoveOutcome.Illegal)
        assertEquals(IllegalReason.OCCUPIED, (outcome as MoveOutcome.Illegal).reason)
    }

    @Test
    fun `single stone capture works`() {
        val game = state(
            listOf(
                ".X.",
                "XO.",
                ".X.",
            ),
        )
        val outcome = GameEngine.play(game, Point(2, 1))
        assertTrue(outcome is MoveOutcome.Success)
        val success = outcome as MoveOutcome.Success
        assertEquals(listOf(Point(1, 1)), success.captured)
        assertEquals(Stone.EMPTY, success.state.board.get(1, 1))
        assertEquals(1, success.state.blackCaptures)
        assertEquals(Stone.WHITE, success.state.toPlay)
        assertNull(success.state.koPoint)
    }

    @Test
    fun `suicide is rejected`() {
        val game = state(
            listOf(
                ".O.",
                "O.O",
                ".O.",
            ),
        )
        val outcome = GameEngine.play(game, Point(1, 1))
        assertTrue(outcome is MoveOutcome.Illegal)
        assertEquals(IllegalReason.SUICIDE, (outcome as MoveOutcome.Illegal).reason)
    }

    @Test
    fun `immediate ko recapture is rejected`() {
        val game = state(
            listOf(
                ".XO..",
                "XO.O.",
                ".XO..",
                ".....",
                ".....",
            ),
            toPlay = Stone.BLACK,
        )
        val capture = GameEngine.play(game, Point(2, 1)) as MoveOutcome.Success
        assertEquals(Point(1, 1), capture.state.koPoint)
        assertEquals(Stone.WHITE, capture.state.toPlay)

        val recapture = GameEngine.play(capture.state, Point(1, 1))
        assertTrue(recapture is MoveOutcome.Illegal)
        assertEquals(IllegalReason.KO, (recapture as MoveOutcome.Illegal).reason)
    }

    @Test
    fun `recapture is allowed after playing elsewhere`() {
        val game = state(
            listOf(
                ".XO..",
                "XO.O.",
                ".XO..",
                ".....",
                ".....",
            ),
        )
        val capture = GameEngine.play(game, Point(2, 1)) as MoveOutcome.Success
        val elsewhere = GameEngine.play(capture.state, Point(4, 4)) as MoveOutcome.Success
        val blackElsewhere = GameEngine.play(elsewhere.state, Point(4, 3)) as MoveOutcome.Success
        assertNull(blackElsewhere.state.koPoint)

        val recapture = GameEngine.play(blackElsewhere.state, Point(1, 1))
        assertTrue(recapture is MoveOutcome.Success)
        val recaptured = recapture as MoveOutcome.Success
        assertEquals(Stone.WHITE, recaptured.state.board.get(1, 1))
        assertEquals(Stone.EMPTY, recaptured.state.board.get(2, 1))
    }

    @Test
    fun `two passes enter scoring`() {
        val game = state(listOf("...", "...", "..."))
        val firstPass = GameEngine.pass(game)
        assertEquals(GamePhase.PLAYING, firstPass.phase)
        assertEquals(1, firstPass.passes)
        val secondPass = GameEngine.pass(firstPass)
        assertEquals(GamePhase.SCORING, secondPass.phase)
        assertEquals(2, secondPass.passes)
    }

    @Test
    fun `snapshot restores previous position`() {
        val start = state(
            listOf(
                "...",
                "...",
                "...",
            ),
        )
        val after = (GameEngine.play(start, Point(1, 1)) as MoveOutcome.Success).state
        val restored = after.history.last().toState(after.history.dropLast(1))
        assertEquals(start.board, restored.board)
        assertEquals(start.toPlay, restored.toPlay)
        assertEquals(start.moveNumber, restored.moveNumber)
        assertTrue(restored.history.isEmpty())
    }

    @Test
    fun `simulation does not append history`() {
        val start = state(
            listOf(
                "...",
                "...",
                "...",
            ),
        )
        val simulated = GameEngine.play(start, Point(1, 1), recordHistory = false)
        assertTrue(simulated is MoveOutcome.Success)
        assertTrue((simulated as MoveOutcome.Success).state.history.isEmpty())

        val recorded = GameEngine.play(start, Point(1, 1)) as MoveOutcome.Success
        assertEquals(1, recorded.state.history.size)
    }

    @Test
    fun `undo history is capped`() {
        var game = GameState(board = Board.empty(19))
        for (index in 0 until 100) {
            val point = Point(index % 19, index / 19)
            val outcome = GameEngine.play(game, point)
            assertTrue("Move $index should be legal", outcome is MoveOutcome.Success)
            game = (outcome as MoveOutcome.Success).state
        }
        assertEquals(GameEngine.MAX_UNDO_PLIES, game.history.size)
    }

    @Test
    fun `legal moves exclude suicide and occupied points`() {
        val game = state(
            listOf(
                ".....",
                "..O..",
                ".O.O.",
                "..O..",
                ".....",
            ),
        )
        val legal = GameEngine.legalMoves(game)
        assertTrue(legal.isNotEmpty())
        assertTrue(legal.none { it == Point(2, 2) })
        assertTrue(legal.none { it == Point(2, 1) })
    }
}

