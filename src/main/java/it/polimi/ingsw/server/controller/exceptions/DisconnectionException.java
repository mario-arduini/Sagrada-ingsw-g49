package it.polimi.ingsw.server.controller.exceptions;

public class DisconnectionException extends Exception{
    public DisconnectionException() { super(); }

    public DisconnectionException(String s){ super(s); }
}