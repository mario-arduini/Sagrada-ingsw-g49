package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.controller.GamesHandler;
import it.polimi.ingsw.network.server.ConnectionHandler;
import it.polimi.ingsw.network.server.Logger;
import it.polimi.ingsw.network.server.exception.LoginFailedException;

import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;

public class Login extends UnicastRemoteObject implements LoginInterface{
    private GamesHandler gamesHandler;

    public Login(GamesHandler gamesHandler) throws java.rmi.RemoteException{
        super();
        this.gamesHandler = gamesHandler;
        try {
            Naming.rebind("rmi:///WidgetFactory",this);
        } catch (Exception e){
            Logger.print("Failure setting up RMI Login: "
                    + e.getMessage());
        }
    }

    @Override
    public void login(String name, String password, ConnectionHandler connectionHandler) throws LoginFailedException {
        if (this.gamesHandler.login(name, password, connectionHandler) == null)
            throw new LoginFailedException();
    }
}
