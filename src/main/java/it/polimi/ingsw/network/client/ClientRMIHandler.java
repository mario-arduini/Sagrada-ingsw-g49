package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.server.rmi.LoginInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClientRMIHandler extends UnicastRemoteObject implements ClientRMIInterface {

    private LoginInterface loginInterface;
    
    ClientRMIHandler(String serverAddress) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(serverAddress);
        this.loginInterface = (LoginInterface)registry.lookup("logger");

        loginInterface.hello("Hello World!", this);
    }

    @Override
    public void salute(String message) throws RemoteException{
        System.out.println(message);
    }
}
