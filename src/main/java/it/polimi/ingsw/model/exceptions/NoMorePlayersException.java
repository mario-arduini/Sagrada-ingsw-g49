package it.polimi.ingsw.model.exceptions;

public class NoMorePlayersException extends Exception {
    public NoMorePlayersException() { super(); }

    public NoMorePlayersException(String s){ super(s); }
}
