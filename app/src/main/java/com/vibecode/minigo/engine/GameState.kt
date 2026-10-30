package com.vibecode.minigo.engine

import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase {
    PLAYING,
    SCORING,
    FINISHED,
}

@Serializable
enum class EndReason {
    NONE,
    TWO_PASSES,
    BLACK_RESIGNED,
    WHITE_RESIGNED,
}

@Serializable
data class Snapshot(
    val board: Board,
    val toPlay: Stone,
    val blackCaptures: Int,
    val whiteCaptures: Int,
    val moveNumber: Int,
    val lastMove: Point?,
    val koPoint: Point?,
    val passes: Int,
    val phase: GamePhase,
    val endReason: EndReason,
    val deadStones: List<Point>,
) {
    fun toState(history: List<Snapshot>): GameState = GameState(
        board = board,
        toPlay = toPlay,
        blackCaptures = blackCaptures,
        whiteCaptures = whiteCaptures,
        moveNumber = moveNumber,
        lastMove = lastMove,
        koPoint = koPoint,
        passes = passes,
        phase = phase,
        endReason = endReason,
        deadStones = deadStones,
        history = history,
    )
}

@Serializable
data class GameState(
    val board: Board,
    val toPlay: Stone = Stone.BLACK,
    val blackCaptures: Int = 0,
    val whiteCaptures: Int = 0,
    val moveNumber: Int = 0,
    val lastMove: Point? = null,
    val koPoint: Point? = null,
    val passes: Int = 0,
    val phase: GamePhase = GamePhase.PLAYING,
    val endReason: EndReason = EndReason.NONE,
    val deadStones: List<Point> = emptyList(),
    val history: List<Snapshot> = emptyList(),
) {
    init {
        require(board.size >= 2) { "Invalid board size" }
    }

    fun snapshot(): Snapshot = Snapshot(
        board = board,
        toPlay = toPlay,
        blackCaptures = blackCaptures,
        whiteCaptures = whiteCaptures,
        moveNumber = moveNumber,
        lastMove = lastMove,
        koPoint = koPoint,
        passes = passes,
        phase = phase,
        endReason = endReason,
        deadStones = deadStones,
    )
}
