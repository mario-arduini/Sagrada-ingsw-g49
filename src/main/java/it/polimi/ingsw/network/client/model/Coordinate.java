package it.polimi.ingsw.network.client.model;

public class Coordinate {
    private final int row;
    private final int column;

    //TODO: have to throw an exception according to schema size
    public Coordinate(int row, int column){
        this.row = row - 1;
        this.column = column - 1;
    }

    public int getRow(){
        return row;
    }

    public int getColumn(){
        return column;
    }
}
