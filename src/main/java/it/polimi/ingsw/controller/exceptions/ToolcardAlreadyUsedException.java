package it.polimi.ingsw.controller.exceptions;

public class ToolcardAlreadyUsedException extends Exception {
    public ToolcardAlreadyUsedException() { super(); }

    public ToolcardAlreadyUsedException(String s){ super(s); }
}
