package it.polimi.ingsw.RmiInterfaces;

import it.polimi.ingsw.server.controller.exceptions.DisconnectionException;
import it.polimi.ingsw.server.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Interface that provides methods to be called on a Client.
 * The methods provided allow a proper communication from the server to the client.
 */
public interface ClientInterface extends Remote, Serializable {

    /**
     * Notify if a player logs in in the lobby.
     * @param nickname of the player who logged in.
     * @throws RemoteException on RMI problems.
     */
    void notifyLogin(String nickname) throws RemoteException;

    /**
     * Notify initial list of player in the lobby.
     * @param nicknames List of players in the lobby.
     * @throws RemoteException on RMI problems.
     */
    void notifyLogin(List<String> nicknames) throws RemoteException;

    /**
     * Notifies if a player logs out.
     * @param nickname of the player who logged out.
     * @throws RemoteException on RMI problems.
     */
    void notifyLogout(String nickname) throws RemoteException;

    /**
     * Notifies initial schemas to choose from.
     * @param schemas List of schemas to choose from.
     * @throws RemoteException on RMI problems.
     */
    void notifySchemas(List<Schema> schemas) throws RemoteException;

    /**
     * Notifies the next round/turn to be played.
     * @param currentPlayer name of the player who is playing.
     * @param draftPool List of dice of the draft pool.
     * @param newRound Boolean true if it's a new round, false otherwise.
     * @param roundTrack List of dice in the round track, null if not a new round.
     * @throws RemoteException on RMI problems.
     */
    void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack) throws RemoteException;

    /**
     * Notifies schemas chosen by every player.
     * @param playersSchemas Map of player's name and his schema.
     * @throws RemoteException on RMI problems.
     */
    void notifyOthersSchemas(Map<String, Schema> playersSchemas) throws RemoteException;

    /**
     * Notifies the placement of a dice.
     * @param nickname of the player who used the toolcard.
     * @param row the row where the dice has been placed.
     * @param column the column where the dice has been placed.
     * @param dice the dice that has been placed.
     * @throws RemoteException on RMI problems.
     */
    void notifyDicePlaced(String nickname, int row, int column, Dice dice) throws RemoteException;

    /**
     * Notifies the usage of a tool card.
     * @param player the name of the player who used the tool card.
     * @param toolCard the name of the tool card used.
     * @param window the new window of the player who used the tool card.
     * @param draftPool List of Dice in the draft pool.
     * @param roundTrack List of Dice in the round track.
     * @throws RemoteException on RMI problems.
     */
    void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack) throws RemoteException;

    /**
     * Notifies information of a game.
     * @param toolCardsMap Map of ToolCard's name and boolean for 'used' flag.
     * @param publicGoals List of name of the public goals.
     * @param privateGoal name of the private goal.
     * @throws RemoteException on RMI problems.
     */
    void notifyGameInfo(Map<String, Boolean> toolCardsMap, List<String> publicGoals, String privateGoal) throws RemoteException;

    /**
     * Notifies information useful during a reconnection.
     * @param windows Map of Player's name and his window.
     * @param favorToken Map of Player's name and his favor tokens.
     * @param roundTrack List of Dice in the round track.
     * @param toolCardName name of the active toolcard, empty string if no toolcard is active
     * @throws RemoteException on RMI problems.
     */
    void notifyReconInfo(Map<String, Window> windows, Map<String, Integer> favorToken, List<Dice> roundTrack, String toolCardName) throws RemoteException;

    /**
     * Notifies the end of a game.
     * @param scores a list containing the scores of the players in the game.
     * @throws RemoteException on RMI problems.
     */
    void notifyEndGame(List<Score> scores) throws RemoteException;

    /**
     * Notifies the suspention of a player.
     * @param nickname of the player suspended.
     * @throws RemoteException on RMI problems.
     */
    void notifySuspension(String nickname) throws RemoteException;



    /**
     * Asks for coordinates of a dice in a window.
     * Returns the coordinates chosen.
     * @param prompt prompt to be displayed.
     * @param rollback true if rollback is possible, false otherwise.
     * @return Coordinate of the Dice in the window.
     * @throws RemoteException on RMI problems.
     * @throws RollbackException if player does not want to play the tool card anymore.
     * @throws DisconnectionException if the player disconnected.
     */
    Coordinate askDiceWindow(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;

    /**
     * Asks the player for a dice in the draft pool.
     * Returns the dice chosen.
     * @param prompt prompt to be displayed.
     * @param rollback true if rollback is possible, false otherwise.
     * @return the Dice chosen.
     * @throws RemoteException on RMI problems.
     * @throws RollbackException if player does not want to play the tool card anymore.
     * @throws DisconnectionException if the player disconnected.
     */
    Dice askDiceDraftPool(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;

    /**
     * Asks the player for a dice in the round-track, returns the position of the chosen dice.
     * @param prompt prompt to be displayed.
     * @param rollback true if rollback is possible, false otherwise.
     * @return an int, position of a dice in the round track.
     * @throws RemoteException on RMI problems.
     * @throws RollbackException if player does not want to play the tool card anymore.
     * @throws DisconnectionException if the player disconnected.
     */
    int askDiceRoundTrack(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;

    /**
     * Asks whether add or subtract a value from a dice.
     * Returns a boolean representing the choice of the player.
     * @param prompt prompt to be displayed.
     * @param rollback true if rollback is possible, false otherwise.
     * @return boolean true if plus, false otherwise.
     * @throws RemoteException on RMI problems.
     * @throws RollbackException if player does not want to play the tool card anymore.
     * @throws DisconnectionException if the player disconnected.
     */
    boolean askIfPlus(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;

    /**
     * Asks a value for a dice.
     * Returns a value for the dice.
     * @param prompt prompt to be displayed.
     * @param rollback true if rollback is possible, false otherwise.
     * @return new value for the dice.
     * @throws RemoteException on RMI problems.
     * @throws RollbackException if player does not want to play the tool card anymore.
     * @throws DisconnectionException if the player disconnected.
     */
    int askDiceValue(String prompt, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;

    /**
     * Asks how many moves player wants to do.
     * Returns the number of moves that the client wants to perform.
     * @param prompt prompt to be displayed.
     * @param number max number of moves.
     * @param rollback true if rollback is possible, false otherwise.
     * @return number of moves that the client wants to perform.
     * @throws RemoteException on RMI problems.
     * @throws RollbackException if player does not want to play the tool card anymore.
     * @throws DisconnectionException if the player disconnected.
     */
    int askMoveNumber(String prompt, int number, boolean rollback) throws RemoteException, RollbackException, DisconnectionException;

    /**
     * Shows a dice.
     * @param dice to be shown.
     * @throws RemoteException on RMI problems.
     */
    void showDice(Dice dice) throws RemoteException;

    /**
     * Notifies that a dice can not be placed in the window and is being put in the draft pool.
     * @param dice dice to be put in the draft pool.
     * @throws RemoteException on RMI problems.
     */
    void alertDiceInDraftPool(Dice dice) throws RemoteException;
}
