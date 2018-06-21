package it.polimi.ingsw.controller.exceptions;

public class GameNotStartedException extends Exception {
    public GameNotStartedException() { super(); }

    public GameNotStartedException(String s){ super(s); }
}
