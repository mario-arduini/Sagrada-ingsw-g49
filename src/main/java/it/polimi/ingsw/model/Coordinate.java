package it.polimi.ingsw.model;

import java.io.Serializable;

/**
 * Class representing a coordinate in a game's window
 */
public class Coordinate implements Serializable {
    private final int row;
    private final int column;

    /**
     * Construct a 0-based coordinate from the row and the column 1-based
     * @param row Integer representing the row of the coordinate
     * @param column Integer representing the column of the coordinate
     */
    public Coordinate(int row, int column){
        this.row = row-1;
        this.column = column-1;
    }

    /**
     * Get the row of the coordinate
     * @return row
     */
    public int getRow(){
        return row;
    }

    /**
     * Get the column of the coordinate
     * @return column
     */
    public int getColumn(){
        return column;
    }
}
