package it.polimi.ingsw.client.exception;

/**
 * This exception is thrown when the connection with the server had gone down and a reconnection was successful
 */
public class ServerReconnectedException extends Exception{
    public ServerReconnectedException() { super(); }

    public ServerReconnectedException(String s){ super(s); }
}