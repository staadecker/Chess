package engine.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class PositionIteratorTest {
    @Test
    fun test() {
        val positionIterator = PositionIterator()

        var i = 0

        while (positionIterator.hasNext()) {
            val position = positionIterator.next()

            when {
                i == 0 -> Assertions.assertEquals(Position(0, 0), position)
                i == 1 -> Assertions.assertEquals(Position(0, 1), position)
                i == LIMIT * LIMIT - 1 -> Assertions.assertEquals(Position(LIMIT - 1, LIMIT - 1), position)
                i >= LIMIT * LIMIT -> throw RuntimeException("Position is too large: " + position.toString())
            }

            i++
        }
    }
}