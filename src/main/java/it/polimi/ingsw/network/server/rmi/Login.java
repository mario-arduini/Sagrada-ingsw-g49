package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.controller.GamesHandler;
import it.polimi.ingsw.network.client.ClientRMIInterface;
import it.polimi.ingsw.network.server.ClientInterface;
import it.polimi.ingsw.network.server.Logger;
import it.polimi.ingsw.network.server.exception.LoginFailedException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Login extends UnicastRemoteObject implements LoginInterface{
    private GamesHandler gamesHandler;

    public Login (GamesHandler gamesHandler) throws RemoteException{
        this.gamesHandler = gamesHandler;
    }

    @Override
    public FlowHandlerInterface login(String name, String password, ClientInterface connectionHandler) throws LoginFailedException, RemoteException {
        return this.gamesHandler.login(name, password, connectionHandler);
    }

    @Override
    public void hello(String message, ClientRMIInterface client) throws RemoteException{
        Logger.print("Someone's knocking at the door.");
        Logger.print(message);
        client.salute(message);
    }
}
