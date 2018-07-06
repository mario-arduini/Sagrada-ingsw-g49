package it.polimi.ingsw.controller.exceptions;

public class ToolCardInUseException extends Exception {
    public ToolCardInUseException() { super(); }

    public ToolCardInUseException(String s){ super(s); }
}
