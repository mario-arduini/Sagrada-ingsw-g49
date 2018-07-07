package it.polimi.ingsw.network.RMIInterfaces;

import it.polimi.ingsw.controller.exceptions.DisconnectionException;
import it.polimi.ingsw.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ClientInterface extends Remote, Serializable {

    void notifyLogin(String nickname) throws RemoteException;
    void notifyLogin(List<String> nicknames) throws RemoteException;
    void notifyLogout(String nickname) throws RemoteException;
    void notifySchemas(List<Schema> schemas) throws RemoteException;
    //TODO: as in gameRoom, maybe overload..??
    void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack) throws RemoteException;
    void notifyOthersSchemas(Map<String, Schema> playersSchemas) throws RemoteException;
    void notifyDicePlaced(String nickname, int row, int column, Dice dice) throws RemoteException;
    void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack) throws RemoteException;
    void notifyGameInfo(List<String> toolCards, List<String> publicGoals, String privateGoal) throws RemoteException;
    void notifyReconInfo(Map<String, Window> windows, Map<String, Integer> favorToken, List<Dice> roundTrack, String toolCardName) throws RemoteException;
    void notifyEndGame(List<Score> scores) throws RemoteException;
    void notifySuspension(String nickname) throws RemoteException;

    //void close();

    Coordinate askDiceWindow(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;
    Dice askDiceDraftPool(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;
    int askDiceRoundTrack(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;
    boolean askIfPlus(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;
    int askDiceValue(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;
    int askMoveNumber(String prompt, int number, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;
    void showDice(Dice dice) throws RemoteException;
    void alertDiceInDraftPool(Dice dice) throws RemoteException;
}
