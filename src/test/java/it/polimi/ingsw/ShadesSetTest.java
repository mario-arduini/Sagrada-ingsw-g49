package it.polimi.ingsw;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.DarkShades;
import it.polimi.ingsw.model.goalcards.LightShades;
import it.polimi.ingsw.model.goalcards.MediumShades;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShadesSetTest {

    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private static final int MIN_DIFFICULTY = 3;

    /*  b5 g3 b5 g4 b5      0
     *  p6 -  -  -  r3      4
     *  g1 -  -  -  -       2
     *  r4 -  -  -  -         */

    private void fillMosaic(Window window){
        try {
            window.addDice(0,0,new Dice(Color.BLUE,5));
            window.addDice(1,0,new Dice(Color.PURPLE,6));
            window.addDice(2,0,new Dice(Color.GREEN,1));
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

    /*  b5  g3  r4  y2  p1      2  1-2 Sets
     *  p6  b5  g1  r6  y4      4  3-4 Sets
     *  g1  r4  y3  p4  g5      4  5-6 Sets
     *  r4  b5  -   -   -        */

    private void fillMosaic2(Window window){
        try {
            window.addDice(0,0,new Dice(Color.BLUE,5));
            window.addDice(1,0,new Dice(Color.PURPLE,6));
            window.addDice(2,0,new Dice(Color.GREEN,1));
            window.addDice(3,0,new Dice(Color.RED,4));
            window.addDice(0,1,new Dice(Color.GREEN,3));
            window.addDice(1,1,new Dice(Color.BLUE,5));
            window.addDice(2,1,new Dice(Color.RED,4));
            window.addDice(3,1,new Dice(Color.BLUE,5));
            window.addDice(0,2,new Dice(Color.RED,4));
            window.addDice(0,3,new Dice(Color.YELLOW,2));
            window.addDice(0,4,new Dice(Color.PURPLE,1));
            window.addDice(1,2,new Dice(Color.GREEN,1));
            window.addDice(1,3,new Dice(Color.RED,6));
            window.addDice(1,4,new Dice(Color.YELLOW,4));
            window.addDice(2,2,new Dice(Color.YELLOW,3));
            window.addDice(2,3,new Dice(Color.PURPLE,4));
            window.addDice(2,4,new Dice(Color.GREEN,5));
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
    void lightShadesTest(){
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PublicGoal goal = new LightShades();

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(0,goal.computeScore(window));
            window = new Window(schema);
            fillMosaic2(window);
            assertEquals(2,goal.computeScore(window));


        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }

    @Test
    void mediumShadesTest(){
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PublicGoal goal = new MediumShades();

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(4,goal.computeScore(window));
            window = new Window(schema);
            fillMosaic2(window);
            assertEquals(4,goal.computeScore(window));


        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }

    @Test
    void darkShadesTest(){
        Schema schema;
        Constraint[][] constraint;
        Window window;
        PublicGoal goal = new DarkShades();

        try {
            //Create window w/ schema
            constraint = new Constraint[ROW][COLUMN];
            schema = new Schema(MIN_DIFFICULTY, constraint);
            window = new Window(schema);
            fillMosaic(window);
            assertEquals(2,goal.computeScore(window));
            window = new Window(schema);
            fillMosaic2(window);
            assertEquals(4,goal.computeScore(window));


        } catch (InvalidDifficultyValueException e) {
            assertTrue(false);
        } catch (UnexpectedMatrixSizeException e) {
            assertTrue(false);
        }

    }

}
