package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PrivateGoal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrivateGoalTest {

    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private static final int MIN_DIFFICULTY = 3;

    private void fillMosaic(Window window){
        try {
            window.addDice(0,0,new Dice(Color.BLUE,5));
            window.addDice(1,0,new Dice(Color.PURPLE,6));
            window.addDice(2,0,new Dice(Color.BLUE,1));
            window.addDice(3,0,new Dice(Color.RED,4));
            window.addDice(0,1,new Dice(Color.GREEN,3));
            window.addDice(0,2,new Dice(Color.BLUE,5));
            window.addDice(0,3,new Dice(Color.GREEN,4));
            window.addDice(0,4,new Dice(Color.BLUE,5));
            window.addDice(1,4,new Dice(Color.RED,3));
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
        }
    }

    @Test
    void redTest(){
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PrivateGoal goal = new PrivateGoal(Color.RED);

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(7,goal.computeScore(window));

        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }

    @Test
    void blueTest(){
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PrivateGoal goal = new PrivateGoal(Color.BLUE);

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(16,goal.computeScore(window));

        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }

    @Test
    void greenTest(){
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PrivateGoal goal = new PrivateGoal(Color.GREEN);

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(7,goal.computeScore(window));

        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }

    @Test
    void purpleTest(){
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PrivateGoal goal = new PrivateGoal(Color.PURPLE);

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(6,goal.computeScore(window));

        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }

    @Test
    void yellowTest(){
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PrivateGoal goal = new PrivateGoal(Color.YELLOW);

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint, SchemaTest.name);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(0,goal.computeScore(window));

        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }
}
