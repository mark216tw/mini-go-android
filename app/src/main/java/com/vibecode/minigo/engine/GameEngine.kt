package com.vibecode.minigo.engine

enum class IllegalReason {
    OCCUPIED,
    SUICIDE,
    KO,
    NOT_PLAYING,
}

sealed interface MoveOutcome {
    data class Success(
        val state: GameState,
        val captured: List<Point>,
    ) : MoveOutcome

    data class Illegal(val reason: IllegalReason) : MoveOutcome
}

object GameEngine {

    const val MAX_UNDO_PLIES = 80

    fun play(
        state: GameState,
        point: Point,
        recordHistory: Boolean = true,
    ): MoveOutcome {
        if (state.phase != GamePhase.PLAYING) {
            return MoveOutcome.Illegal(IllegalReason.NOT_PLAYING)
        }
        if (!state.board.inBounds(point)) {
            return MoveOutcome.Illegal(IllegalReason.OCCUPIED)
        }
        if (!state.board.isEmpty(point)) {
            return MoveOutcome.Illegal(IllegalReason.OCCUPIED)
        }
        if (state.koPoint == point) {
            return MoveOutcome.Illegal(IllegalReason.KO)
        }

        val color = state.toPlay
        val opponent = color.opponent()
        var board = state.board.place(point, color)
        val captured = ArrayList<Point>()

        for (neighbor in state.board.neighbors(point)) {
            if (board.get(neighbor) != opponent) continue
            val group = board.groupAt(neighbor) ?: continue
            if (group.liberties.isEmpty()) {
                captured.addAll(group.stones)
                board = board.remove(group.stones)
            }
        }

        val ownGroup = board.groupAt(point)
        if (ownGroup == null || ownGroup.liberties.isEmpty()) {
            return MoveOutcome.Illegal(IllegalReason.SUICIDE)
        }

        val koPoint = if (
            captured.size == 1 &&
            ownGroup.stones.size == 1 &&
            ownGroup.liberties.size == 1
        ) {
            captured.first()
        } else {
            null
        }

        val next = state.copy(
            board = board,
            toPlay = opponent,
            blackCaptures = state.blackCaptures + if (color == Stone.BLACK) captured.size else 0,
            whiteCaptures = state.whiteCaptures + if (color == Stone.WHITE) captured.size else 0,
            moveNumber = state.moveNumber + 1,
            lastMove = point,
            koPoint = koPoint,
            passes = 0,
            phase = GamePhase.PLAYING,
            endReason = EndReason.NONE,
            deadStones = emptyList(),
            history = appendHistory(state, recordHistory),
        )
        return MoveOutcome.Success(next, captured)
    }

    fun pass(state: GameState): GameState {
        if (state.phase != GamePhase.PLAYING) return state
        val passes = state.passes + 1
        val nextPhase = if (passes >= 2) GamePhase.SCORING else GamePhase.PLAYING
        return state.copy(
            toPlay = state.toPlay.opponent(),
            moveNumber = state.moveNumber + 1,
            lastMove = null,
            koPoint = null,
            passes = passes,
            phase = nextPhase,
            endReason = EndReason.NONE,
            deadStones = emptyList(),
            history = appendHistory(state),
        )
    }

    fun toggleDeadGroup(state: GameState, point: Point): GameState {
        if (state.phase != GamePhase.SCORING) return state
        val group = state.board.groupAt(point) ?: return state
        val dead = state.deadStones.toMutableSet()
        if (dead.containsAll(group.stones)) {
            dead.removeAll(group.stones)
        } else {
            dead.addAll(group.stones)
        }
        return state.copy(deadStones = dead.sortedWith(compareBy({ it.y }, { it.x })))
    }

    fun cancelScoring(state: GameState): GameState {
        if (state.phase != GamePhase.SCORING) return state
        return state.copy(
            phase = GamePhase.PLAYING,
            passes = 0,
            deadStones = emptyList(),
        )
    }

    fun confirmScoring(state: GameState): GameState {
        if (state.phase != GamePhase.SCORING) return state
        return state.copy(
            phase = GamePhase.FINISHED,
            endReason = EndReason.TWO_PASSES,
        )
    }

    fun resign(state: GameState, by: Stone): GameState {
        if (state.phase != GamePhase.PLAYING) return state
        val reason = if (by == Stone.BLACK) EndReason.BLACK_RESIGNED else EndReason.WHITE_RESIGNED
        return state.copy(
            phase = GamePhase.FINISHED,
            endReason = reason,
            koPoint = null,
        )
    }

    fun legalMoves(state: GameState): List<Point> {
        if (state.phase != GamePhase.PLAYING) return emptyList()
        val moves = ArrayList<Point>()
        for (y in 0 until state.board.size) {
            for (x in 0 until state.board.size) {
                val point = Point(x, y)
                if (state.board.get(point) != Stone.EMPTY) continue
                if (state.koPoint == point) continue
                if (play(state, point, recordHistory = false) is MoveOutcome.Success) moves.add(point)
            }
        }
        return moves
    }

    private fun appendHistory(
        state: GameState,
        recordHistory: Boolean = true,
    ): List<Snapshot> {
        if (!recordHistory) return state.history
        val appended = state.history + state.snapshot()
        return if (appended.size > MAX_UNDO_PLIES) {
            appended.takeLast(MAX_UNDO_PLIES)
        } else {
            appended
        }
    }
}
