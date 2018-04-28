package it.polimi.ingsw.model;

public class Schema {
    private final int difficulty;
    private final Constraint[][] constraint;

    public Schema(int difficulty, Constraint[][] constraint){
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
