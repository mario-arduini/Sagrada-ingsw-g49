package it.polimi.ingsw.model.exceptions;

public class NotEnoughDiceToMoveException extends Exception {
    public NotEnoughDiceToMoveException() { super(); }

    public NotEnoughDiceToMoveException(String s){ super(s); }
}
