package it.polimi.ingsw;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DiceTest {

    @Test
    void diceTest(){

        final Dice dice = new Dice(Color.RED);
        assertTrue(dice.getValue() == 0 && dice.getColor() == Color.RED);

        assertThrows(InvalidDiceValueException.class, () -> dice.setValue(7));
        assertTrue(dice.getValue() == 0 && dice.getColor() == Color.RED);

        assertDoesNotThrow(() -> dice.setValue(6));
        assertTrue(dice.getValue() == 6 && dice.getColor() == Color.RED);

        assertDoesNotThrow(() -> dice.setValue(1));
        assertTrue(dice.getValue() == 1 && dice.getColor() == Color.RED);

        assertThrows(InvalidDiceValueException.class, () -> dice.setValue(0));
        assertThrows(InvalidDiceValueException.class, () -> dice.setValue(-20));
        assertTrue(dice.getValue() == 1 && dice.getColor() == Color.RED);

        dice.roll();
        assertTrue(dice.getValue() >= 1 && dice.getValue() <= 6 && dice.getColor() == Color.RED);

        AtomicReference<Dice> dice1 = new AtomicReference<>();
        assertDoesNotThrow(() -> dice1.getAndSet(new Dice(Color.GREEN, 3)));
        assertTrue(dice1.get().getValue() == 3 && dice1.get().getColor() == Color.GREEN);

        assertThrows(InvalidDiceValueException.class, () -> dice1.getAndSet(new Dice(Color.GREEN, 10)));
        assertTrue(dice1.get().getValue() == 3 && dice1.get().getColor() == Color.GREEN);

        assertDoesNotThrow(() -> dice1.getAndSet(new Dice(Color.RED, dice.getValue())));
        assertEquals(dice, dice1.get());

        dice1.getAndSet(new Dice(dice));
        assertEquals(dice, dice1.get());
    }
}
