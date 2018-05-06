package it.polimi.ingsw;

import it.polimi.ingsw.model.Color;
import it.polimi.ingsw.model.Constraint;
import it.polimi.ingsw.model.exceptions.InvalidConstraintValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstraintTest {

    @Test
    void constraintTest(){
        Constraint constraint = new Constraint(Color.RED);
        assertTrue(constraint.getColor() == Color.RED && constraint.getNumber() == 0);

        try {
            constraint = new Constraint(5);
            assertTrue(constraint.getColor() == null && constraint.getNumber() == 5);
        } catch (InvalidConstraintValueException e) {
            assertTrue(false);
        }

        try {
            constraint = new Constraint(0);
            assertTrue(false);
        } catch (InvalidConstraintValueException e) {
            assertTrue(constraint.getColor() == null && constraint.getNumber() == 5);
        }

        try {
            constraint = new Constraint(7);
            assertTrue(false);

        } catch (InvalidConstraintValueException e) {
            assertTrue(constraint.getColor() == null && constraint.getNumber() == 5);
        }
    }
}
