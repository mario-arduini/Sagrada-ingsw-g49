package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ClientInterface extends Remote {
    void notifyLogin(String nickname) throws RemoteException;
    void notifyLogin(List<String> nicknames) throws RemoteException;
    void notifyLogout(String nickname) throws RemoteException;
    void notifySchemas(List<Schema> schemas) throws RemoteException;
    void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack) throws RemoteException;
    void notifyOthersSchemas(Map<String, Schema> playersSchemas) throws RemoteException;
    void notifyDicePlaced(String nickname, int row, int column, Dice dice) throws RemoteException;
    void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack) throws RemoteException;
    void notifyGameInfo(List<String> toolCards, List<String> publicGoals, String privateGoal) throws RemoteException;
    void notifyReconInfo(HashMap<String, Window> windows, HashMap<String, Integer> favorToken, List<Dice> roundTrack) throws RemoteException;
    void notifyEndGame(List<Score> scores) throws RemoteException;

    //void close();

    Coordinate askDiceWindow(String prompt) throws RemoteException;
    Dice askDiceDraftPool(String prompt) throws RemoteException;
    int askDiceRoundTrack(String prompt) throws RemoteException;
    boolean askIfPlus(String prompt) throws RemoteException;
    int askDiceValue(String prompt) throws RemoteException;
}