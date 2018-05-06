package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidDifficultyValueException;
import it.polimi.ingsw.model.exceptions.UnexpectedMatrixSizeException;

public class Schema {
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
}
