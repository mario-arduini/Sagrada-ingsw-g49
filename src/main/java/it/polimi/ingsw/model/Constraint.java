package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidConstraintValueException;

import java.io.Serializable;

/**
 * Class representing a single constraint in a schema
 */
public final class Constraint implements Serializable {
    private final Color color;
    private final int number;

    /**
     * Color constraint
     * @param color Color of the constraint
     */
    public Constraint(Color color) {
        this.color = color;
        this.number = 0;
    }

    /**
     * Value constrainte
     * @param number Integer between 1 and 6, value of the constraint
     * @throws InvalidConstraintValueException Signals an invalid dice value for the constraint
     */
    public Constraint(int number) throws InvalidConstraintValueException {
        if(number < 1 || number > 6)
            throw  new InvalidConstraintValueException();

        this.color = null;
        this.number = number;
    }

    /**
     * Get Color of the constraint or null if it is a Value constraint
     * @return Color of the constraint or null
     */
    public Color getColor(){

        return color;
    }

    /**
     * Get value of the constraint or 0 if it is a Color constraint
     * @return Integer between 0 and 6
     */
    public Integer getNumber(){

        return number;
    }

    /**
     * Confront constraints
     * @param constraint Constraint to confront with the caller
     * @return true if equals, false otherwise
     */
    @Override
    public boolean equals(Object constraint) {
        return constraint instanceof Constraint && this.color == ((Constraint) constraint).color && this.number == ((Constraint) constraint).number;
    }
}
