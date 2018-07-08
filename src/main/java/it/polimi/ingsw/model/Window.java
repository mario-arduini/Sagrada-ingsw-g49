package it.polimi.ingsw.model;
import it.polimi.ingsw.model.exceptions.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class representing the window of a player and the current state of his mosaic
 */
public class Window implements Serializable {
    public static final int ROW = 4;
    public static final int COLUMN = 5;
    private final Schema schema;
    private Dice[][] mosaic;
    private boolean firstDicePlaced;

    public enum RuleIgnored{ COLOR, NUMBER, ADJACENCIES, NONE }

    /**
     * Duplicate a Window given
     * @param window the window to recreate
     */
    public Window(Window window){
        this.schema = window.schema;
        this.mosaic = new Dice[ROW][COLUMN];
        this.firstDicePlaced = false;

        for(int i = 0; i < ROW; i++)
            for(int j = 0; j < COLUMN; j++)
                if(window.getCell(i, j) != null){
                    firstDicePlaced = true;
                    this.mosaic[i][j] = new Dice(window.getCell(i, j));
                }
    }


    /**
     * Create an empty window given a Schema
     * @param schema Schema of the window
     */
    public Window(Schema schema){
        this.schema = schema;
        this.mosaic = new Dice[ROW][COLUMN];
        this.firstDicePlaced = false;
    }

    /**
     * Get the schema of this window
     * @return schema corresponding to the window
     */
    public Schema getSchema(){

        return this.schema;
    }

    /**
     * Get the content of a cell given row and column
     * @param row row of the cell
     * @param column column of the cell
     * @return The content of the cell that is a Dice or null if cell is empty
     */
    public Dice getCell(int row, int column){
        return mosaic[row][column];
    }

    /**
     * Remove the dice from the cell if present
     * @param row row of the cell
     * @param column column of the cell
     */
    public void removeDice(int row,int column){
        mosaic[row][column] = null;
        if (Arrays.stream(mosaic).flatMap(Arrays::stream).noneMatch(Objects::nonNull)) {
            firstDicePlaced = false;
        }
    }

    /**
     * Add the Dice in the specified cell to the windows verifying all the constraints
     * @param row row of the cell
     * @param column column of the cell
     * @param dice Dice to be added
     * @throws ConstraintViolatedException signals that a constraint of the schema was not respected
     * @throws FirstDiceMisplacedException signals that the first was not place in the proper position (border of the window)
     * @throws NoAdjacentDiceException signals that a non-first dice is placed not adjacent to another dice
     * @throws BadAdjacentDiceException signals that one of the orthogonal was of the same color or value of dice
     * @throws DiceAlreadyHereException signals that the cell is already occupied by another dice
     */
    public void addDice(int row, int column, Dice dice) throws ConstraintViolatedException, FirstDiceMisplacedException, NoAdjacentDiceException, BadAdjacentDiceException, DiceAlreadyHereException {
        Constraint constraint = schema.getConstraint(row, column);
        checkColorConstraint(constraint, dice);
        checkValueConstraint(constraint, dice);
        checkPlacementConstraint(row, column, dice);

        setDice(row,column,dice);
    }

    /**
     * check the placement constraint for dice in cell specified
     * @param row row of the cell
     * @param column column of the cell
     * @param dice Dice to be added
     * @throws FirstDiceMisplacedException signals that the first was not place in the proper position (border of the window)
     * @throws NoAdjacentDiceException signals that a non-first dice is placed not adjacent to another dice
     * @throws BadAdjacentDiceException signals that one of the orthogonal was of the same color or value of dice
     * @throws DiceAlreadyHereException signals that the cell is already occupied by another dice
     */
    public void checkPlacementConstraint(int row, int column, Dice dice) throws FirstDiceMisplacedException, NoAdjacentDiceException, BadAdjacentDiceException, DiceAlreadyHereException {
        if (!firstDicePlaced) {
            checkBorder(row, column);
        } else {
            if (getCell(row,column)!=null) throw new DiceAlreadyHereException();
            checkAdjacencies(row, column, dice);
        }
    }

    /**
     * tells if the first dice is already placed
     * @return true if there is at least one Dice in the window, false otherwise
     */
    public boolean isFirstDicePlaced() {
        return firstDicePlaced;
    }

    /**
     * Set the dice in the position specified, no check is made
     * @param row row of the cell
     * @param column column of the cell
     * @param dice Dice to be added
     */
    public void setDice(int row, int column, Dice dice){
        mosaic[row][column] = new Dice(dice);
        this.firstDicePlaced = true;
    }

    /**
     * Check if the Dice is being placed on the border
     * @param row row of the cell
     * @param column column of the cell
     * @throws FirstDiceMisplacedException signals Dice is not being placed on the border of the window
     */
    private void checkBorder(int row, int column) throws FirstDiceMisplacedException {
        if(row != 0 && column != 0 && row != ROW - 1 && column != COLUMN - 1)
            throw new FirstDiceMisplacedException();
    }

    /**
     * Check if the dice is respecting a Color constraint
     * @param constraint Constraint to check
     * @param dice Dice to check
     * @throws ConstraintViolatedException signals Dice is not respecting the constraint
     */
    public void checkColorConstraint(Constraint constraint, Dice dice) throws ConstraintViolatedException {
        if (constraint != null && constraint.getColor() != null && dice.getColor() != constraint.getColor())
            throw new ConstraintViolatedException();
    }

    /**
     * Check if the dice is respecting a Value constraint
     * @param constraint Constraint to check
     * @param dice Dice to check
     * @throws ConstraintViolatedException signals Dice is not respecting the constraint
     */
    public void checkValueConstraint(Constraint constraint, Dice dice) throws ConstraintViolatedException {
        if (constraint != null && constraint.getNumber() != 0 && !dice.getValue().equals(constraint.getNumber()))
            throw new ConstraintViolatedException();
    }

    /**
     * Check if Dice in the given cell is respecting placement restrictions
     * @param row row of the cell
     * @param column column of the cell
     * @param dice Dice to check
     * @throws NoAdjacentDiceException signals there is no Dice close to the cell
     * @throws BadAdjacentDiceException signals there is an orthogonal Dice to the cell with the same value or Color
     */
    public void checkAdjacencies(int row, int column, Dice dice) throws NoAdjacentDiceException, BadAdjacentDiceException {
        Color color = dice.getColor();
        int value = dice.getValue();
        boolean adjacencyFlag = false;

        if(checkUpAdjacencies(row,column,color,value))
            adjacencyFlag = true;

        if(checkDownAdjacencies(row,column,color, value))
            adjacencyFlag = true;

        if(checkSideAdjacencies(row,column,color, value))
            adjacencyFlag = true;

        if (!adjacencyFlag) throw new NoAdjacentDiceException();
    }

    /**
     * Check placement restriction on the above cells
     * @param row row of the cell
     * @param column column of the cell
     * @param color Color to check in the cell
     * @param value value to check in the cell
     * @return true if exists a Dice in the above cells, false otherwise
     * @throws BadAdjacentDiceException signals there is an orthogonal Dice to the cell with the same value or Color
     */
    private boolean checkUpAdjacencies(int row, int column, Color color, int value) throws BadAdjacentDiceException{
        boolean adjacencyFlag = false;
        Dice tmp;
        if (row >= 1) {
            tmp = mosaic[row - 1][column];
            if (tmp != null) {
                if (tmp.getColor() == color || tmp.getValue() == value)
                    throw new BadAdjacentDiceException();
                else
                    adjacencyFlag = true;
            }
            if (column >= 1) {
                tmp = mosaic[row - 1][column - 1];
                if (tmp != null)
                    adjacencyFlag = true;
            }
            if (column <= COLUMN - 2) {
                tmp = mosaic[row - 1][column + 1];
                if (tmp != null)
                    adjacencyFlag = true;
            }
        }
        return adjacencyFlag;
    }

    /**
     * Check placement restriction on the below cells
     * @param row row of the cell
     * @param column column of the cell
     * @param color Color to check in the cell
     * @param value value to check in the cell
     * @return true if exists a Dice in the below cells, false otherwise
     * @throws BadAdjacentDiceException signals there is an orthogonal Dice to the cell with the same value or Color
     */
    private boolean checkDownAdjacencies(int row, int column, Color color, int value) throws BadAdjacentDiceException{
        boolean adjacencyFlag = false;
        Dice tmp;
        if (row <= ROW - 2){
            //Check on dice down (Bad Adjacency)
            tmp = mosaic[row + 1][column];
            if (tmp != null) {
                if (tmp.getColor() == color || tmp.getValue() == value)
                    throw new BadAdjacentDiceException();
                else
                    adjacencyFlag = true;
            }
            //Check on dice down-left
            if (column >= 1) {
                tmp = mosaic[row + 1][column - 1];
                if (tmp != null)
                    adjacencyFlag = true;
            }
            //Check on dice down-right
            if (column <= COLUMN - 2) {
                tmp = mosaic[row + 1][column + 1];
                if (tmp != null)
                    adjacencyFlag = true;
            }
        }
        return adjacencyFlag;
    }

    /**
     * Check placement restriction on the side cells
     * @param row row of the cell
     * @param column column of the cell
     * @param color Color to check in the cell
     * @param value value to check in the cell
     * @return true if exists a Dice in the side cells, false otherwise
     * @throws BadAdjacentDiceException signals there is an orthogonal Dice to the cell with the same value or Color
     */
    private boolean checkSideAdjacencies(int row, int column, Color color, int value) throws BadAdjacentDiceException{
        boolean adjacencyFlag = false;
        Dice tmp;
        if (column >= 1){
            //Check on dice left (Bad Adjacency)
            tmp = mosaic[row][column-1];
            if (tmp != null) {
                if (tmp.getColor() == color || tmp.getValue() == value)
                    throw new BadAdjacentDiceException();
                else
                    adjacencyFlag = true;
            }
        }

        if (column <= COLUMN - 2){
            //Check on dice right (Bad Adjacency)
            tmp = mosaic[row][column+1];
            if (tmp != null) {
                if (tmp.getColor() == color || tmp.getValue() == value)
                    throw new BadAdjacentDiceException();
                else
                    adjacencyFlag = true;
            }
        }
        return adjacencyFlag;
    }

    /**
     * Check if the given Window is equals to the caller
     * @param window Window to check
     * @return true if equals, false otherwise
     */
    @Override
    public boolean equals(Object window) {

        if(!(window instanceof Window))
            return false;
        for(int i = 0; i < ROW; i++)
            for (int j = 0; j < COLUMN; j++){
                if ((this.mosaic[i][j] != null && ((Window) window).getCell(i, j) == null) || (this.mosaic[i][j] == null && ((Window) window).getCell(i, j) != null))
                    return false;
                if (this.mosaic[i][j] != null && ((Window) window).getCell(i, j) != null && !this.mosaic[i][j].equals(((Window) window).getCell(i, j)))
                    return false;
        }

        return this.schema.equals(((Window)window).getSchema());
    }

    /**
     * Check if it is possible to place a Dice in the given cell, possibly ignoring some restriction
     * @param dice Dice to be added
     * @param row row of the cell
     * @param column column of the cell
     * @param ruleIgnored Rule to ignore
     * @throws ConstraintViolatedException signals that a constraint of the schema was not respected
     * @throws FirstDiceMisplacedException signals that the first was not place in the proper position (border of the window)
     * @throws NoAdjacentDiceException signals that a non-first dice is placed not adjacent to another dice
     * @throws BadAdjacentDiceException signals that one of the orthogonal was of the same color or value of dice
     * @throws DiceAlreadyHereException signals that the cell is already occupied by another dice
     */
    public void canBePlaced(Dice dice,int row,int column,RuleIgnored ruleIgnored) throws ConstraintViolatedException, FirstDiceMisplacedException, NoAdjacentDiceException, BadAdjacentDiceException, DiceAlreadyHereException {
        Constraint constraint = getSchema().getConstraint(row, column);
        switch (ruleIgnored){
            case COLOR:
                checkValueConstraint(constraint,dice);
                checkPlacementConstraint(row, column, dice);
                break;
            case NUMBER:
                checkColorConstraint(constraint,dice);
                checkPlacementConstraint(row, column, dice);
                break;
            case ADJACENCIES:
                checkValueConstraint(constraint, dice);
                checkColorConstraint(constraint, dice);
                break;
            case NONE:
                checkColorConstraint(constraint, dice);
                checkValueConstraint(constraint, dice);
                checkPlacementConstraint(row, column, dice);
                break;
        }
    }

    /**
     * Find the number of possible cells in which a Dice can be placed, possibly ignoring some restrictions
     * @param dice Dice to check
     * @param ruleIgnored Rule to ignore
     * @return Number of possible cells in the window in which the Dice can be placed
     */
    public int possiblePlaces(Dice dice,RuleIgnored ruleIgnored){
        int possiblePlaces = 0;
        for(int r=0;r<ROW;r++)
            for(int c=0;c<COLUMN;c++){
                try{
                    canBePlaced(dice,r,c,ruleIgnored);
                    possiblePlaces++;
                } catch (NoAdjacentDiceException | BadAdjacentDiceException
                        | FirstDiceMisplacedException | ConstraintViolatedException | DiceAlreadyHereException e) {
                }
            }
        return possiblePlaces;
    }

    /**
     * Find the number of empty cells in the Window
     * @return Number of empty cells in the Window
     */
    int getEmptySpaces(){
        int count = 0;
        for (Dice[] aMosaic : mosaic)
            for (int j = 0; j < mosaic[0].length; j++)
                if (aMosaic[j] == null)
                    count++;
        return count;
    }

    /**
     * Find the number of Dices currently placed in the Window
     * @return Number of Dice currently placed in the Window
     */
    public int numOfDicePlaced(){
        int count = 0;
        for (Dice[] aMosaic : mosaic)
            for (int j = 0; j < mosaic[0].length; j++)
                if (aMosaic[j] != null)
                    count++;
        return count;
    }
}
