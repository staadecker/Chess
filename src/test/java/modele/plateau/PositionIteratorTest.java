package modele.plateau;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PositionIteratorTest {
    @Test
    void test() {
        PositionIterator positionIterator = new PositionIterator();

        int i = 0;

        while (positionIterator.hasNext()) {
            Position position = positionIterator.next();

            if (i == 0) {
                Assertions.assertEquals(new Position(0, 0), position);
            } else if (i == 1) {
                Assertions.assertEquals(new Position(0, 1), position);
            } else if (i == Position.getLimite() * Position.getLimite() - 1) {
                Assertions.assertEquals(new Position(Position.getLimite() - 1, Position.getLimite() - 1), position);
            } else if (i >= Position.getLimite() * Position.getLimite()) {
                throw new RuntimeException("Position trop grande: " + position.toString());
            }

            i++;
        }
    }
}