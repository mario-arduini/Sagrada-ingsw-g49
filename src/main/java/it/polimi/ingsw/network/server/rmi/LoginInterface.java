package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.network.server.ConnectionHandler;
import it.polimi.ingsw.network.server.exception.LoginFailedException;

public interface LoginInterface extends java.rmi.Remote{
    void login(String name, String password, ConnectionHandler connectionHandler) throws java.rmi.RemoteException, LoginFailedException;
}
