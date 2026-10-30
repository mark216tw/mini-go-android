package com.vibecode.minigo.engine

import kotlin.math.abs

data class ScoreResult(
    val blackStones: Int,
    val whiteStones: Int,
    val blackTerritory: Int,
    val whiteTerritory: Int,
    val neutral: Int,
    val komi: Double,
    val blackScore: Double,
    val whiteScore: Double,
    val winner: Stone?,
    val margin: Double,
)

object Scoring {
    const val KOMI = 6.5

    fun result(state: GameState): ScoreResult {
        val board = if (state.deadStones.isEmpty()) {
            state.board
        } else {
            state.board.remove(state.deadStones)
        }

        var blackStones = 0
        var whiteStones = 0
        for (stone in board.cells) {
            when (stone) {
                Stone.BLACK -> blackStones++
                Stone.WHITE -> whiteStones++
                Stone.EMPTY -> Unit
            }
        }

        var blackTerritory = 0
        var whiteTerritory = 0
        var neutral = 0
        val visited = BooleanArray(board.size * board.size)

        for (y in 0 until board.size) {
            for (x in 0 until board.size) {
                val start = Point(x, y)
                if (visited[board.index(x, y)] || board.get(start) != Stone.EMPTY) continue

                val region = ArrayList<Point>()
                val borders = HashSet<Stone>()
                val pending = ArrayDeque<Point>()
                pending.add(start)
                visited[board.index(x, y)] = true

                while (pending.isNotEmpty()) {
                    val current = pending.removeFirst()
                    region.add(current)
                    for (neighbor in board.neighbors(current)) {
                        val stone = board.get(neighbor)
                        if (stone == Stone.EMPTY) {
                            val index = board.index(neighbor.x, neighbor.y)
                            if (!visited[index]) {
                                visited[index] = true
                                pending.add(neighbor)
                            }
                        } else {
                            borders.add(stone)
                        }
                    }
                }

                when {
                    borders.size == 1 && borders.contains(Stone.BLACK) -> blackTerritory += region.size
                    borders.size == 1 && borders.contains(Stone.WHITE) -> whiteTerritory += region.size
                    else -> neutral += region.size
                }
            }
        }

        val blackScore = (blackStones + blackTerritory).toDouble()
        val whiteScore = (whiteStones + whiteTerritory).toDouble() + KOMI
        val difference = blackScore - whiteScore
        val winner = when {
            abs(difference) < 1e-9 -> null
            difference > 0 -> Stone.BLACK
            else -> Stone.WHITE
        }

        return ScoreResult(
            blackStones = blackStones,
            whiteStones = whiteStones,
            blackTerritory = blackTerritory,
            whiteTerritory = whiteTerritory,
            neutral = neutral,
            komi = KOMI,
            blackScore = blackScore,
            whiteScore = whiteScore,
            winner = winner,
            margin = abs(difference),
        )
    }
}
