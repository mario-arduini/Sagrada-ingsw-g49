package it.polimi.ingsw.model;
import com.sun.tools.internal.jxc.ap.Const;
import it.polimi.ingsw.model.exceptions.*;

public class Window {
    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private final Schema schema;
    private Dice[][] mosaic;
    private boolean firstDice;

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

    public void addDice(int row, int column, Dice dice) throws ConstraintViolatedException, FirstDiceMisplacedException, NoAdjacentDiceException, BadAdjacentDiceException {
        Constraint constraint = schema.getConstraint(row, column);

        if (!firstDice) {
            checkBorder(row, column);
            checkConstraint(constraint, dice);
            this.firstDice = true;
        }
        else {
            checkConstraint(constraint, dice);
            checkAdjacencies(row,column, dice);
        }

        this.mosaic[row][column] = new Dice(dice);
    }

    private void checkBorder(int row, int column) throws FirstDiceMisplacedException {
        if (row != 0 && row != ROW - 1)
            throw new FirstDiceMisplacedException();

        if (column != 0 && column != COLUMN - 1)
            throw new FirstDiceMisplacedException();
    }

    private void checkConstraint(Constraint constraint, Dice dice) throws ConstraintViolatedException {
        if (constraint != null)
            if (constraint.getColor() != null && dice.getColor() != constraint.getColor())
                throw new ConstraintViolatedException();
            else if(constraint.getNumber() != 0 && dice.getValue() != constraint.getNumber())
                throw new ConstraintViolatedException();

    }

    private void checkAdjacencies(int row, int column, Dice dice) throws NoAdjacentDiceException, BadAdjacentDiceException {
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

}
