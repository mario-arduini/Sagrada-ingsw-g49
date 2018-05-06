package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class WindowTest {
    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private static final int MIN_DIFFICULTY = 3;

    @Test
    void testWindowCostructor(){
        Schema schema;
        Constraint[][] constraint;
        Window window;
        Dice dice;

        //Create window w/ schema
        try {
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint);
            window = new Window(schema);
            assertEquals(schema, window.getSchema());
        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

        //Create window w/ schema and constraints, check for every cell
        try {
            constraint = new Constraint[ROW][COLUMN];
            constraint[ROW/2][COLUMN/2] = new Constraint(6);
            schema = new Schema(MIN_DIFFICULTY, constraint);
            window = new Window(schema);
            assertEquals(schema, window.getSchema());

            for (int i = 0; i < constraint.length; i++)
                for (int j = 0; j<constraint[0].length ; j++)
                    assertEquals(null, window.getCell(i,j));

        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        } catch (InvalidConstraintValueException e) {
            assertTrue(false);
        }
    }

    @Test
    void tesAddDice() {
        Schema schema;
        Constraint[][] constraint;
        Window window;
        Dice dice;


        try {
            constraint = new Constraint[ROW][COLUMN];
            constraint[1][0] = new Constraint(6);
            dice = new Dice(Color.RED, 3);
            schema = new Schema(3, constraint);
            window = new Window(schema);

            //Add first dice in the middle of the mosaic
            try {
                window.addDice(ROW / 2, COLUMN / 2, dice);
                assertTrue(false);
            } catch (NoAdjacentDiceException e) {
                assertTrue(false);
            } catch (BadAdjacentDiceException e) {
                assertTrue(false);
            } catch (FirstDiceMisplacedException e) {
                assertTrue(true);
            } catch (ConstraintViolatedException e) {
                assertTrue(false);
            }

            //Add first dice in the first position the mosaic
            try {
                window.addDice(0, 0, dice);
                assertEquals(dice, window.getCell(0, 0));
            } catch (NoAdjacentDiceException e) {
                assertTrue(false);
            } catch (BadAdjacentDiceException e) {
                assertTrue(false);
            } catch (FirstDiceMisplacedException e) {
                assertTrue(false);
            } catch (ConstraintViolatedException e) {
                assertTrue(false);
            }

            //Place a non-adjacent dice
            try {
                window.addDice(ROW - 1, COLUMN - 1, dice);
                assertTrue(false);
            } catch (NoAdjacentDiceException e) {
                assertTrue(true);
            } catch (BadAdjacentDiceException e) {
                assertTrue(false);
            } catch (FirstDiceMisplacedException e) {
                assertTrue(false);
            } catch (ConstraintViolatedException e) {
                assertTrue(false);
            }

            //Place a Bad-adjacent dice (Color)
            try {
                dice.setValue(4);
                window.addDice(0, 1, dice);
                assertTrue(false);
            } catch (NoAdjacentDiceException e) {
                assertTrue(false);
            } catch (BadAdjacentDiceException e) {
                assertTrue(true);
            } catch (FirstDiceMisplacedException e) {
                assertTrue(false);
            } catch (ConstraintViolatedException e) {
                assertTrue(false);
            }

            //Place a Bad-adjacent dice (Value)
            try {
                dice = new Dice(Color.GREEN, 3);
                window.addDice(0, 1, dice);
                assertTrue(false);
            } catch (NoAdjacentDiceException e) {
                assertTrue(false);
            } catch (BadAdjacentDiceException e) {
                assertTrue(true);
            } catch (FirstDiceMisplacedException e) {
                assertTrue(false);
            } catch (ConstraintViolatedException e) {
                assertTrue(false);
            }

            //Place a good adjacent dice (diagonal)
            try {
                window.addDice(1, 1, dice);
                assertEquals(dice, window.getCell(1, 1));
            } catch (NoAdjacentDiceException e) {
                assertTrue(false);
            } catch (BadAdjacentDiceException e) {
                assertTrue(false);
            } catch (FirstDiceMisplacedException e) {
                assertTrue(false);
            } catch (ConstraintViolatedException e) {
                assertTrue(false);
            }

            //Place a bad-constraint dice
            try {
                window.addDice(1, 0, dice);
                assertTrue(false);
            } catch (NoAdjacentDiceException e) {
                assertTrue(false);
            } catch (BadAdjacentDiceException e) {
                assertTrue(false);
            } catch (FirstDiceMisplacedException e) {
                assertTrue(false);
            } catch (ConstraintViolatedException e) {
                assertTrue(true);
            }

            //Place a bad adjacent dice
            try {
                dice.setValue(6);
                window.addDice(1, 0, dice);
                assertTrue(false);
            } catch (NoAdjacentDiceException e) {
                assertTrue(false);
            } catch (BadAdjacentDiceException e) {
                assertTrue(true);
            } catch (FirstDiceMisplacedException e) {
                assertTrue(false);
            } catch (ConstraintViolatedException e) {
                assertTrue(false);
            }

            //Place a good constraint, good adjacent dice (orthogonal)
            try {
                dice = new Dice(Color.PURPLE, 6);
                window.addDice(1, 0, dice);
                assertEquals(dice, window.getCell(1,0));
            } catch (NoAdjacentDiceException e) {
                assertTrue(false);
            } catch (BadAdjacentDiceException e) {
                assertTrue(false);
            } catch (FirstDiceMisplacedException e) {
                assertTrue(false);
            } catch (ConstraintViolatedException e) {
                assertTrue(false);
            }

        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (InvalidDiceValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        } catch (InvalidConstraintValueException e) {
            assertTrue(false);
        }
    }
}

