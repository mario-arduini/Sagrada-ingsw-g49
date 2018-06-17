package it.polimi.ingsw.network.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientRMIHandler extends UnicastRemoteObject implements ClientRMIInterface {


    public ClientRMIHandler() throws RemoteException {
        
    }

    public void salute(String message){
        System.out.println(message);
    }
}
