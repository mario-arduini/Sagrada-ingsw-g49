package it.polimi.ingsw.model.exceptions;

public class ConstraintViolatedException extends Exception {
    public ConstraintViolatedException() { super(); }

    public ConstraintViolatedException(String s){ super(s); }
}
