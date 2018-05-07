package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidDifficultyValueException;
import it.polimi.ingsw.model.exceptions.UnexpectedMatrixSizeException;

public final class Schema {
    private final int difficulty;
    private final Constraint[][] constraint;
    private static final int ROW = 4;
    private static final int COLUMN = 5;

    public Schema(int difficulty, Constraint[][] constraint) throws InvalidDifficultyValueException, UnexpectedMatrixSizeException {
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
    }

    public int getDifficulty() {

        return difficulty;
    }

    public Constraint getConstraint(int row, int column) {

        return constraint[row][column];
    }

    @Override
    public boolean equals(Object schema) {

        if(!(schema instanceof Schema))
            return false;


        for (int i = 0; i < ROW; i++)
            for(int j = 0; j < COLUMN; j++)
                if(!this.constraint[i][j].equals(((Schema)schema).getConstraint(i,j)))
                    return  false;

        return this.difficulty == ((Schema) schema).getDifficulty();
    }
}
