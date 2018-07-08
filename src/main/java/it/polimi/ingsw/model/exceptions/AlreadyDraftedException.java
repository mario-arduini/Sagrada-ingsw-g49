package it.polimi.ingsw.model.exceptions;

public class AlreadyDraftedException extends Exception {
    public AlreadyDraftedException() { super(); }

    public AlreadyDraftedException(String s){ super(s); }
}
