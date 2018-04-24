package it.polimi.ingsw.model;

public class Window {
    private final Schema schema;
    private Dice[][] mosaic;

    public Window(Schema schema){
        this.schema = schema;
        this.mosaic = new Dice[4][5];
    }

    public Schema getSchema(){
        return this.schema;
    }

    public Dice getCell(int row, int column){
        return mosaic[row][column];
    }


    public boolean addDice(int row, int column, Dice dice){
        if(mosaic[row][column] == null)
            return false;
        else
            this.mosaic[row][column] = dice;
            return true;
    }
}
