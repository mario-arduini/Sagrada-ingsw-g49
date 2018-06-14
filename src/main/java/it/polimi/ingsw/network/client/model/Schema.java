package it.polimi.ingsw.network.client.model;

public final class Schema {
    private final int difficulty;
    private final Constraint[][] constraint;
    private final String name;
    private static final int ROW = 4;
    private static final int COLUMN = 5;

    public Schema(int difficulty, Constraint[][] constraint, String name) {
        this.difficulty = difficulty;
        this.constraint = constraint;
        this.name = name;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getName(){
        return name;
    }

    public Constraint getConstraint(int row, int column) {
        return constraint[row][column];
    }

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
