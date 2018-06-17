package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.server.rmi.LoginInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClientRMIHandler extends UnicastRemoteObject implements ClientRMIInterface {


    public ClientRMIHandler() throws RemoteException, NotBoundException {
        
    }

    public void salute(String message){
        System.out.println(message);
    }
}
