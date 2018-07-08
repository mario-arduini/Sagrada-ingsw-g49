package it.polimi.ingsw.server.rmi;

import it.polimi.ingsw.server.controller.GamesHandler;
import it.polimi.ingsw.RmiInterfaces.ClientInterface;
import it.polimi.ingsw.RmiInterfaces.FlowHandlerInterface;
import it.polimi.ingsw.RmiInterfaces.LoginInterface;
import it.polimi.ingsw.server.Logger;
import it.polimi.ingsw.server.exception.LoginFailedException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Login class provides an RMI entry-point to the game.
 * A login action allows a client to receive a GameFlowHandler.
 */
public class Login extends UnicastRemoteObject implements LoginInterface {
    private GamesHandler gamesHandler;

    /**
     * Creates a Login object.
     * @param gamesHandler the gamesHandler to whom players can login to.
     * @throws RemoteException on RMI problems.
     */
    public Login (GamesHandler gamesHandler) throws RemoteException{
        this.gamesHandler = gamesHandler;
    }

    @Override
    public FlowHandlerInterface login(String name, String password, ClientInterface connectionHandler) throws LoginFailedException, RemoteException {
        Logger.print("Connection over Rmi: " + name);
        return this.gamesHandler.login(name, password, connectionHandler);
    }
}
