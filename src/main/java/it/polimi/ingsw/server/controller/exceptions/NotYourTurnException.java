package it.polimi.ingsw.server.controller.exceptions;

public class NotYourTurnException extends Exception{
    public NotYourTurnException() { super(); }

    public NotYourTurnException(String s){ super(s); }
}