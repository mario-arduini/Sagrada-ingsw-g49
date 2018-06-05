package it.polimi.ingsw.model;

public class Coordinate {
    private final int row;
    private final int column;

    Coordinate(int row, int column){
        this.row = row;
        this.column = column;
    }

    public int getRow(){
        return row;
    }

    public int getColumn(){
        return column;
    }
}
