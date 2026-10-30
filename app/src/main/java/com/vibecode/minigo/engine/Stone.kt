package com.vibecode.minigo.engine

import kotlinx.serialization.Serializable

@Serializable
enum class Stone {
    EMPTY,
    BLACK,
    WHITE,
    ;

    fun opponent(): Stone = when (this) {
        BLACK -> WHITE
        WHITE -> BLACK
        EMPTY -> EMPTY
    }
}
