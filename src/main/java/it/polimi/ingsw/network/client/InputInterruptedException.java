package it.polimi.ingsw.network.client;

/**
 * This exception is thrown when the waiting for an input from the user was interrupted
 */
public class InputInterruptedException extends Exception{

    public InputInterruptedException() { super(); }

    public InputInterruptedException(String s){ super(s); }
}