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

        AtomicReference<Dice> dice = new AtomicReference<>();
        dice.getAndSet(new Dice(Color.RED));
        assertTrue(dice.get().getValue() == 0 && dice.get().getColor() == Color.RED);

        assertThrows(InvalidDiceValueException.class, () -> dice.get().setValue(7));
        assertTrue(dice.get().getValue() == 0 && dice.get().getColor() == Color.RED);

        assertDoesNotThrow(() -> dice.get().setValue(6));
        assertTrue(dice.get().getValue() == 6 && dice.get().getColor() == Color.RED);

        assertDoesNotThrow(() -> dice.get().setValue(1));
        assertTrue(dice.get().getValue() == 1 && dice.get().getColor() == Color.RED);

        assertThrows(InvalidDiceValueException.class, () -> dice.get().setValue(0));
        assertThrows(InvalidDiceValueException.class, () -> dice.get().setValue(-20));
        assertTrue(dice.get().getValue() == 1 && dice.get().getColor() == Color.RED);

        dice.get().roll();
        assertTrue(dice.get().getValue() >= 1 && dice.get().getValue() <= 6 && dice.get().getColor() == Color.RED);

        AtomicReference<Dice> dice1 = new AtomicReference<>();
        assertDoesNotThrow(() -> dice1.getAndSet(new Dice(Color.GREEN, 3)));
        assertTrue(dice1.get().getValue() == 3 && dice1.get().getColor() == Color.GREEN);

        assertThrows(InvalidDiceValueException.class, () -> dice1.getAndSet(new Dice(Color.GREEN, 10)));
        assertTrue(dice1.get().getValue() == 3 && dice1.get().getColor() == Color.GREEN);

        assertDoesNotThrow(() -> dice1.getAndSet(new Dice(Color.RED, dice.get().getValue())));
        assertEquals(dice.get(), dice1.get());

        assertDoesNotThrow(() -> dice1.getAndSet(new Dice(Color.GREEN, dice.get().getValue())));
        assertNotEquals(dice.get(), dice1.get());

        assertNotEquals(dice.get(), new Object());

        dice1.getAndSet(new Dice(dice.get()));
        assertEquals(dice.get(), dice1.get());
    }
}
