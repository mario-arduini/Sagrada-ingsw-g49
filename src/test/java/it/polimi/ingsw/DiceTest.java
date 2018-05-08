package it.polimi.ingsw;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiceTest {

    @Test
    void diceTest(){

        Dice dice = new Dice(Color.RED);
        assertTrue(dice.getValue() == 0 && dice.getColor() == Color.RED);

        try {
            dice.setValue(7);
            assertTrue(false);
        } catch (InvalidDiceValueException e) {
            assertTrue(dice.getValue() == 0 && dice.getColor() == Color.RED);
        }

        try {
            dice.setValue(6);
            assertTrue(dice.getValue() == 6 && dice.getColor() == Color.RED);
        } catch (InvalidDiceValueException e) {
            assertTrue(false);
        }

        try {
            dice.setValue(1);
            assertTrue(dice.getValue() == 1 && dice.getColor() == Color.RED);
        } catch (InvalidDiceValueException e) {
            assertTrue(false);
        }

        try {
            dice.setValue(0);
            assertTrue(false);
        } catch (InvalidDiceValueException e) {
            assertTrue(dice.getValue() == 1 && dice.getColor() == Color.RED);
        }

        try {
            dice.setValue(-20);
            assertTrue(false);
        } catch (InvalidDiceValueException e) {
            assertTrue(dice.getValue() == 1 && dice.getColor() == Color.RED);
        }

        dice.roll();
        assertTrue(dice.getValue() >= 1 && dice.getValue() <= 6 && dice.getColor() == Color.RED);

        try {
            dice = new Dice(Color.GREEN, 3);
            assertTrue(dice.getValue() == 3 && dice.getColor() == Color.GREEN);
        } catch (InvalidDiceValueException e) {
            assertTrue(false);
        }

        try {
            dice = new Dice(Color.GREEN, 10);
            assertTrue(false);
        } catch (InvalidDiceValueException e) {
            assertTrue(dice.getValue() == 3 && dice.getColor() == Color.GREEN);
        }
    }
}
