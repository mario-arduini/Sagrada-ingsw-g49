package it.polimi.ingsw.server.controller.exceptions;

public class GameOverException extends Exception {
    public GameOverException() { super(); }

    public GameOverException(String s){ super(s); }
}