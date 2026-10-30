package com.vibecode.minigo.ui.common

import com.vibecode.minigo.engine.Point

private const val COORDINATE_LETTERS = "ABCDEFGHJKLMNOPQRST"

fun Point.toGoCoordinate(boardSize: Int): String {
    if (x !in 0 until boardSize || y !in 0 until boardSize) return "?"
    if (x !in COORDINATE_LETTERS.indices) return "?"
    return "${COORDINATE_LETTERS[x]}${boardSize - y}"
}
