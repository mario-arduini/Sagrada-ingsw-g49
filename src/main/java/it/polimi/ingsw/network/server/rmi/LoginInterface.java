package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.network.client.ClientRMIInterface;
import it.polimi.ingsw.network.server.ConnectionHandler;
import it.polimi.ingsw.network.server.exception.LoginFailedException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LoginInterface extends Remote {
    //void login(String name, String password, ConnectionHandler connectionHandler) throws RemoteException, LoginFailedException;
    void hello(String message) throws RemoteException;
}
