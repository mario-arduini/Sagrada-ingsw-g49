package it.polimi.ingsw.rmiInterfaces;

import it.polimi.ingsw.server.exception.LoginFailedException;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Rmi Interface that expose login function.
 */
public interface LoginInterface extends Remote {
    /**
     * This method logs the user in a waiting room or in the game he was already playing.
     * Several notifications happen during this action.
     * @param name Player's name.
     * @param password Token for reconnection.
     * @param connectionHandler Reference to the client in order to perform notifies.
     * @return FlowHandlerInterface
     * @throws RemoteException on RMI problems.
     * @throws LoginFailedException if a user with the same name exists in a game.
     */
    FlowHandlerInterface login(String name, String password, ClientInterface connectionHandler) throws RemoteException, LoginFailedException;
}
