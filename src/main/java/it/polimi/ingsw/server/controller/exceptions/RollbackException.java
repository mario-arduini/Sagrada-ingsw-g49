package it.polimi.ingsw.server.controller.exceptions;

public class RollbackException extends Exception {
    public RollbackException() { super(); }

    public RollbackException(String s){ super(s); }
}
