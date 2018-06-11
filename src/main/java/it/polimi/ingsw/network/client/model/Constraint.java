package it.polimi.ingsw.network.client.model;

import it.polimi.ingsw.network.client.model.exception.InvalidConstraintValueException;

public final class Constraint {
    private final Color color;
    private final int number;

    public Constraint(Color color) {
        this.color = color;
        this.number = 0;
    }

    public Constraint(int number) throws InvalidConstraintValueException {
        if(number < 1 || number > 6)
            throw  new InvalidConstraintValueException();

        this.color = null;
        this.number = number;
    }

    public Color getColor(){
        return color;
    }

    public int getNumber(){
        return number;
    }

    @Override
    public boolean equals(Object constraint) {
        return constraint instanceof Constraint && this.color == ((Constraint) constraint).color && this.number == ((Constraint) constraint).number;
    }
}
