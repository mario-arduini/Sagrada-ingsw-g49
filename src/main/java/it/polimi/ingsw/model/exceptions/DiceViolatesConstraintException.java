package it.polimi.ingsw.model.exceptions;

public class DiceViolatesConstraintException extends Exception {
    public DiceViolatesConstraintException() { super(); }

    public DiceViolatesConstraintException(String s){ super(s); }
}
