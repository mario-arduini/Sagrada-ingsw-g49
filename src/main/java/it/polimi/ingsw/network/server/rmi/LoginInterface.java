package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.network.client.ClientRMIInterface;
import it.polimi.ingsw.network.server.ClientInterface;
import it.polimi.ingsw.network.server.exception.LoginFailedException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LoginInterface extends Remote {
    FlowHandlerInterface login(String name, String password, ClientInterface connectionHandler) throws RemoteException, LoginFailedException;
    void hello(String message, ClientRMIInterface client) throws RemoteException;
}
