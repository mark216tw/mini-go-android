package com.vibecode.minigo.ui.common

import com.vibecode.minigo.engine.Point
import org.junit.Assert.assertEquals
import org.junit.Test

class GoCoordinatesTest {

    @Test
    fun `coordinates start at bottom left and skip i`() {
        assertEquals("A1", Point(0, 8).toGoCoordinate(9))
        assertEquals("J1", Point(8, 8).toGoCoordinate(9))
        assertEquals("D6", Point(3, 3).toGoCoordinate(9))
        assertEquals("D10", Point(3, 3).toGoCoordinate(13))
    }

    @Test
    fun `out of range coordinates return question mark`() {
        assertEquals("?", Point(9, 0).toGoCoordinate(9))
        assertEquals("?", Point(-1, 0).toGoCoordinate(9))
    }
}
