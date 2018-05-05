package it.polimi.ingsw;


import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstJUnitTest{

    @Test
    void diceTest(){

        Dice d = new Dice(Color.RED);
        assertTrue(d.getValue() >= 0 && d.getValue() <= 6);

        try {
            d.setValue(7);
            assertEquals(Boolean.FALSE, d.getValue() == 7);
        } catch (InvalidDiceValueException e) {
            assertTrue(d.getValue() >= 0 && d.getValue() <= 6);
        }
    }
}