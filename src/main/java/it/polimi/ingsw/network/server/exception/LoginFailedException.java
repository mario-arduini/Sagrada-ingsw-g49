package it.polimi.ingsw.network.server.exception;

public class LoginFailedException extends Exception {
    public LoginFailedException() { super(); }

    public LoginFailedException(String s){ super(s); }
}
