package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.DiagonalColor;
import it.polimi.ingsw.model.goalcards.FullColorVariety;
import it.polimi.ingsw.model.goalcards.FullShadeVariety;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FullSetTest {

    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private static final int MIN_DIFFICULTY = 3;

    /*  b5 g3 b5 g4 b5      0
     *  p6 -  -  -  r3      0
     *  g1 -  -  -  -       0
     *  r4 -  -  -  -         */

    private void fillMosaic(Window window) {
        try {
            window.addDice(0, 0, new Dice(Color.BLUE, 5));
            window.addDice(1, 0, new Dice(Color.PURPLE, 6));
            window.addDice(2, 0, new Dice(Color.GREEN, 1));
            window.addDice(3, 0, new Dice(Color.RED, 4));
            window.addDice(0, 1, new Dice(Color.GREEN, 3));
            window.addDice(0, 2, new Dice(Color.BLUE, 5));
            window.addDice(0, 3, new Dice(Color.GREEN, 4));
            window.addDice(0, 4, new Dice(Color.BLUE, 5));
            window.addDice(1, 4, new Dice(Color.RED, 3));
        } catch (ConstraintViolatedException e) {
            e.printStackTrace();
        } catch (FirstDiceMisplacedException e) {
            e.printStackTrace();
        } catch (NoAdjacentDiceException e) {
            e.printStackTrace();
        } catch (BadAdjacentDiceException e) {
            e.printStackTrace();
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        } catch (DiceAlreadyHereException e) {
            e.printStackTrace();
        }
    }

    /*  b5  g3  r4  y2  p1      12 full colors set
     *  p6  b5  g1  r6  y4      5  full shades set
     *  g1  r4  y3  p4  g5      10 diagonal
     *  r4  b5  -   -   -        */

    private void fillMosaic2(Window window) {
        try {
            window.addDice(0, 0, new Dice(Color.BLUE, 5));
            window.addDice(1, 0, new Dice(Color.PURPLE, 6));
            window.addDice(2, 0, new Dice(Color.GREEN, 1));
            window.addDice(3, 0, new Dice(Color.RED, 4));
            window.addDice(0, 1, new Dice(Color.GREEN, 3));
            window.addDice(1, 1, new Dice(Color.BLUE, 5));
            window.addDice(2, 1, new Dice(Color.RED, 4));
            window.addDice(3, 1, new Dice(Color.BLUE, 5));
            window.addDice(0, 2, new Dice(Color.RED, 4));
            window.addDice(0, 3, new Dice(Color.YELLOW, 2));
            window.addDice(0, 4, new Dice(Color.PURPLE, 1));
            window.addDice(1, 2, new Dice(Color.GREEN, 1));
            window.addDice(1, 3, new Dice(Color.RED, 6));
            window.addDice(1, 4, new Dice(Color.YELLOW, 4));
            window.addDice(2, 2, new Dice(Color.YELLOW, 3));
            window.addDice(2, 3, new Dice(Color.PURPLE, 4));
            window.addDice(2, 4, new Dice(Color.GREEN, 5));
        } catch (ConstraintViolatedException e) {
            e.printStackTrace();
        } catch (FirstDiceMisplacedException e) {
            e.printStackTrace();
        } catch (NoAdjacentDiceException e) {
            e.printStackTrace();
        } catch (BadAdjacentDiceException e) {
            e.printStackTrace();
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        } catch (DiceAlreadyHereException e) {
            e.printStackTrace();
        }
    }

    @Test
    void fullColorTest() {
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PublicGoal goal = new FullColorVariety();

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(0, goal.computeScore(window));
            window = new Window(schema);
            fillMosaic2(window);
            assertEquals(12, goal.computeScore(window));


        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }

    @Test
    void fullShadesTest() {
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PublicGoal goal = new FullShadeVariety();

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(0, goal.computeScore(window));
            window = new Window(schema);
            fillMosaic2(window);
            assertEquals(5, goal.computeScore(window));


        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }

    @Test
    void diagonalTest() {
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PublicGoal goal = new DiagonalColor();

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(0, goal.computeScore(window));
            window = new Window(schema);
            fillMosaic2(window);
            assertEquals(10, goal.computeScore(window));


        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }
}