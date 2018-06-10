package it.polimi.ingsw.network.client;

class Coordinate {
    private final int row;
    private final int column;

    //TODO: have to throw an exception according to schema size
    Coordinate(int row, int column){
        this.row = row-1;
        this.column = column-1;
    }

    int getRow(){
        return row;
    }

    int getColumn(){
        return column;
    }
}
