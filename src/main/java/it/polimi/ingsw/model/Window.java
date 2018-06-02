package it.polimi.ingsw.model;
import it.polimi.ingsw.model.exceptions.*;

public class Window {
    public static final int ROW = 4;
    public static final int COLUMN = 5;
    private final Schema schema;
    private Dice[][] mosaic;
    private boolean firstDice;

    public Window(Window window){
        this.schema = window.schema;
        this.mosaic = new Dice[ROW][COLUMN];
        this.firstDice = false;

        for(int i = 0; i < ROW; i++)
            for(int j = 0; j < COLUMN; j++)
                if(window.getCell(i, j) != null){
                    firstDice = true;
                    this.mosaic[i][j] = new Dice(window.getCell(i, j));
                }
    }

    public Window(Schema schema){
        this.schema = schema;
        this.mosaic = new Dice[ROW][COLUMN];
        this.firstDice = false;
    }

    public Schema getSchema(){

        return this.schema;
    }

    public Dice getCell(int row, int column){
        return mosaic[row][column];
    }

    public void removeDice(int row,int column){
        mosaic[row][column] = null;
    }

    public void addDice(int row, int column, Dice dice) throws ConstraintViolatedException, FirstDiceMisplacedException, NoAdjacentDiceException, BadAdjacentDiceException {
        Constraint constraint = schema.getConstraint(row, column);

        if (!firstDice) {
            checkBorder(row, column);
            checkColorConstraint(constraint, dice);
            checkValueConstraint(constraint, dice);
            this.firstDice = true;
        }
        else {
            checkColorConstraint(constraint, dice);
            checkValueConstraint(constraint, dice);
            checkAdjacencies(row,column, dice);
        }

        setDice(row,column,dice);
    }

    public void setDice(int row,int column, Dice dice){
        mosaic[row][column] = new Dice(dice);
    }

    private void checkBorder(int row, int column) throws FirstDiceMisplacedException {
        if (row != 0 && row != ROW - 1)
            throw new FirstDiceMisplacedException();

        if (column != 0 && column != COLUMN - 1)
            throw new FirstDiceMisplacedException();
    }

    public void checkColorConstraint(Constraint constraint, Dice dice) throws ConstraintViolatedException {
        if (constraint != null)
            if (constraint.getColor() != null && dice.getColor() != constraint.getColor())
                throw new ConstraintViolatedException();
    }

    public void checkValueConstraint(Constraint constraint, Dice dice) throws ConstraintViolatedException {
        if (constraint != null)
           if(constraint.getNumber() != 0 && dice.getValue() != constraint.getNumber())
                throw new ConstraintViolatedException();
    }

    public void checkAdjacencies(int row, int column, Dice dice) throws NoAdjacentDiceException, BadAdjacentDiceException {
        Color color = dice.getColor();
        int value = dice.getValue();
        boolean adjacencyFlag = false;
        Dice tmp;

        if(checkUpAdjacencies(row,column,color,value))
            adjacencyFlag = true;


        if(checkDownAdjacencies(row,column,color, value))
            adjacencyFlag = true;

        if(checkSideAdjacencies(row,column,color, value))
            adjacencyFlag = true;

        if (!adjacencyFlag) throw new NoAdjacentDiceException();
    }

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
            if (column < COLUMN - 2) {
                tmp = mosaic[row - 1][column + 1];
                if (tmp != null)
                    adjacencyFlag = true;
            }
        }
        return adjacencyFlag;
    }

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
            if (column < COLUMN - 2) {
                tmp = mosaic[row + 1][column + 1];
                if (tmp != null)
                    adjacencyFlag = true;
            }
        }
        return adjacencyFlag;
    }

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
}
