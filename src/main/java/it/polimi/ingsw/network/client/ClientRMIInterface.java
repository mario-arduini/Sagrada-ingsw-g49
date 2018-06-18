package it.polimi.ingsw.network.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRMIInterface extends Remote{
    void salute(String message) throws RemoteException;
}
