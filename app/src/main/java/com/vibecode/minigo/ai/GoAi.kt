package com.vibecode.minigo.ai

import com.vibecode.minigo.engine.Board
import com.vibecode.minigo.engine.GameEngine
import com.vibecode.minigo.engine.GamePhase
import com.vibecode.minigo.engine.GameState
import com.vibecode.minigo.engine.MoveOutcome
import com.vibecode.minigo.engine.Point
import com.vibecode.minigo.engine.Stone
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object GoAi {

    private const val BUDGET_9X9_MS = 700L
    private const val BUDGET_13X13_MS = 1500L

    fun chooseMove(
        state: GameState,
        level: AiLevel,
        timeBudgetMillis: Long = if (state.board.size >= 13) BUDGET_13X13_MS else BUDGET_9X9_MS,
        shouldStop: () -> Boolean = { false },
    ): Point? {
        if (state.phase != GamePhase.PLAYING) return null
        val deadline = System.nanoTime() + timeBudgetMillis * 1_000_000L

        fun outOfTime(): Boolean = shouldStop() || System.nanoTime() >= deadline

        val color = state.toPlay
        val legal = GameEngine.legalMoves(state)
        if (legal.isEmpty() || shouldStop()) return null

        val candidates = ArrayList<Point>(legal.size)
        for (point in legal) {
            if (!isTrueEye(state.board, point, color)) candidates.add(point)
        }
        if (candidates.isEmpty()) return null

        val scored = ArrayList<Pair<Point, Double>>(candidates.size)
        for (point in candidates) {
            if (outOfTime()) break
            scored.add(point to staticScore(state, point, color))
        }
        if (scored.isEmpty()) return candidates.first()

        val sorted = scored.sortedByDescending { it.second }
        return when (level) {
            AiLevel.BEGINNER -> {
                if (Random.nextFloat() < 0.35f) {
                    candidates.random()
                } else {
                    val take = max(1, sorted.size / 2)
                    sorted.take(take).random().first
                }
            }

            AiLevel.EASY -> sorted.first().first

            AiLevel.MEDIUM -> {
                var bestPoint = sorted.first().first
                var bestValue = Double.NEGATIVE_INFINITY
                for ((point, base) in sorted.take(min(10, sorted.size))) {
                    if (outOfTime()) break
                    val value = base - opponentThreat(state, point, color, ::outOfTime)
                    if (value > bestValue) {
                        bestValue = value
                        bestPoint = point
                    }
                }
                bestPoint
            }
        }
    }

    private fun staticScore(state: GameState, point: Point, color: Stone): Double {
        val outcome = GameEngine.play(state, point, recordHistory = false) as? MoveOutcome.Success
            ?: return -1000.0
        val next = outcome.state
        val opponent = color.opponent()
        var score = outcome.captured.size * 14.0

        for (neighbor in state.board.neighbors(point)) {
            if (state.board.get(neighbor) != color) continue
            val before = state.board.groupAt(neighbor) ?: continue
            if (before.liberties.size == 1) {
                val after = next.board.groupAt(neighbor)
                if (after != null && after.liberties.size > 1) score += 9.0
            }
            score += 1.2
        }

        val checked = HashSet<Point>()
        for (neighbor in next.board.neighbors(point)) {
            if (next.board.get(neighbor) != opponent) continue
            val group = next.board.groupAt(neighbor) ?: continue
            if (!checked.addAll(group.stones)) continue
            if (group.liberties.size == 1) {
                score += 6.0 + if (group.stones.size > 1) 1.5 else 0.0
            }
        }

        val ownAfter = next.board.groupAt(point)
        if (ownAfter != null && ownAfter.liberties.size == 1 && outcome.captured.isEmpty()) {
            score -= 16.0
        }

        val center = (state.board.size - 1) / 2.0
        score += (abs(point.x - center) + abs(point.y - center)) * 0.15

        val edgeDistance = minOf(
            point.x,
            point.y,
            state.board.size - 1 - point.x,
            state.board.size - 1 - point.y,
        )
        when (edgeDistance) {
            0 -> score -= 1.5
            1 -> score -= 0.3
        }

        state.lastMove?.let { last ->
            if (abs(last.x - point.x) + abs(last.y - point.y) == 1) score += 0.8
        }

        score += Random.nextDouble(0.0, 1.5)
        return score
    }

    private fun opponentThreat(
        state: GameState,
        point: Point,
        color: Stone,
        shouldStop: () -> Boolean,
    ): Double {
        if (shouldStop()) return 0.0
        val outcome = GameEngine.play(state, point, recordHistory = false) as? MoveOutcome.Success
            ?: return 0.0
        val next = outcome.state
        val opponent = color.opponent()
        var best = 0.0

        for (reply in GameEngine.legalMoves(next)) {
            if (shouldStop()) break
            val replyOutcome = GameEngine.play(next, reply, recordHistory = false)
                as? MoveOutcome.Success ?: continue
            var threat = replyOutcome.captured.size * 14.0
            val checked = HashSet<Point>()
            for (neighbor in replyOutcome.state.board.neighbors(reply)) {
                if (shouldStop()) break
                if (replyOutcome.state.board.get(neighbor) != color) continue
                val group = replyOutcome.state.board.groupAt(neighbor) ?: continue
                if (!checked.addAll(group.stones)) continue
                if (group.liberties.size == 1) {
                    threat += 6.0 + if (group.stones.size > 1) 1.5 else 0.0
                }
            }
            if (threat > best) best = threat
        }
        return best * 0.7
    }

    private fun isTrueEye(board: Board, point: Point, color: Stone): Boolean {
        if (!board.isEmpty(point)) return false
        val orthogonal = board.neighbors(point)
        if (orthogonal.any { board.get(it) != color }) return false
        val diagonals = board.diagonals(point)
        val friendly = diagonals.count { board.get(it) == color }
        return friendly >= diagonals.size - 1
    }
}
