package it.polimi.ingsw.model.exceptions;

public class DiceNotInDraftPoolException extends Exception {
    public DiceNotInDraftPoolException() { super(); }

    public DiceNotInDraftPoolException(String s){ super(s); }
}
