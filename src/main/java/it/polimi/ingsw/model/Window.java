package it.polimi.ingsw.model;
import it.polimi.ingsw.model.exceptions.ConstraintViolatedException;

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

    public void addDice(int row, int column, Dice dice) throws ConstraintViolatedException {
        Constraint constraint = schema.getConstraint(row, column);
        if (constraint != null && (constraint.getColor() == dice.getColor() || constraint.getNumber() == dice.getValue()))
            throw new ConstraintViolatedException();
        else
            this.mosaic[row][column] = dice;
    }
}
