package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidDifficultyValueException;
import it.polimi.ingsw.model.exceptions.UnexpectedMatrixSizeException;

import java.io.Serializable;

/**
 * Class representing a Schema of the Game
 */
public final class Schema implements Serializable {
    private final int difficulty;
    private final Constraint[][] constraint;
    private static final int ROW = 4;
    private static final int COLUMN = 5;
    private final String name;

    /**
     * Create a Schema from its difficulty, a matrix of Constraint and its Name
     * @param difficulty difficulty of the Schema
     * @param constraint Matrix of the Constraints
     * @param name Name of the Schema
     * @throws InvalidDifficultyValueException signals an invalid Difficulty
     * @throws UnexpectedMatrixSizeException signals an invalid Constraints Matrix
     */
    public Schema(int difficulty, Constraint[][] constraint, String name) throws InvalidDifficultyValueException, UnexpectedMatrixSizeException {
        if (difficulty < 3 || difficulty > 6)
            throw new InvalidDifficultyValueException();
        if (constraint.length != ROW)
            throw new UnexpectedMatrixSizeException();
        for (Constraint[] aConstraint : constraint) {
            if (aConstraint.length != COLUMN)
                throw new UnexpectedMatrixSizeException();
        }
        this.difficulty = difficulty;
        this.constraint = constraint;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    /**
     * Get difficulty of the Schema
     * @return difficulty value
     */
    public int getDifficulty() {

        return difficulty;
    }

    /**
     * Get constraint of given cell
     * @param row row of the cell
     * @param column column of the cell
     * @return Constraint of the Cell if present or null otherwise
     */
    public Constraint getConstraint(int row, int column) {

        return constraint[row][column];
    }

    /**
     * Compare the given Schema to the caller
     * @param schema Schema to compare
     * @return true if the Schemas are equal, false otherwise
     */
    @Override
    public boolean equals(Object schema) {

        if(!(schema instanceof Schema))
            return false;

        for (int i = 0; i < ROW; i++)
            for(int j = 0; j < COLUMN; j++) {
                if((this.constraint[i][j] != null && ((Schema) schema).constraint[i][j] == null) || (this.constraint[i][j] == null && ((Schema) schema).constraint[i][j] != null))
                    return false;
                if (this.constraint[i][j] != null && ((Schema) schema).constraint[i][j] != null && !this.constraint[i][j].equals(((Schema) schema).constraint[i][j]))
                    return false;
            }
        return this.difficulty == ((Schema) schema).difficulty;
    }
}
