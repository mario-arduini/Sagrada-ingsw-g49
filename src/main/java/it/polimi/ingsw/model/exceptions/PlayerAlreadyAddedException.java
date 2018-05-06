package it.polimi.ingsw.model.exceptions;

public class PlayerAlreadyAddedException extends Exception {
    public PlayerAlreadyAddedException() { super(); }

    public PlayerAlreadyAddedException(String s){ super(s); }
}
