package it.polimi.ingsw.model;
import it.polimi.ingsw.model.exceptions.DiceViolatesConstraintException;
import it.polimi.ingsw.model.exceptions.NoSuchWindowCellException;

public class Window {
    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private final Schema schema;
    private Dice[][] mosaic;

    public Window(Schema schema){
        this.schema = schema;
        this.mosaic = new Dice[ROW][COLUMN];
    }

    public Schema getSchema(){

        return this.schema;
    }

    public Dice getCell(int row, int column){

        return mosaic[row][column];
    }

    public void addDice(int row, int column, Dice dice) throws NoSuchWindowCellException, DiceViolatesConstraintException {
        if(row >= ROW || column >= COLUMN)
            throw  new NoSuchWindowCellException();
//        else if(mosaic[row][column] == null)
//            return false;
        else {
            Constraint constraint = schema.getConstraint(row, column);
            if (constraint.getColor() == dice.getColor() || constraint.getNumber() == dice.getValue())
                throw new DiceViolatesConstraintException();
            else
                this.mosaic[row][column] = dice;
        }
    }
}
