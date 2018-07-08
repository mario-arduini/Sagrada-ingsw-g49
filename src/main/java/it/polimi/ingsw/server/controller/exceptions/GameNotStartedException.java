package it.polimi.ingsw.server.controller.exceptions;

public class GameNotStartedException extends Exception {
    public GameNotStartedException() { super(); }

    public GameNotStartedException(String s){ super(s); }
}
