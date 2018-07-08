package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.NoMorePlayersException;
import it.polimi.ingsw.RmiInterfaces.ClientInterface;
import it.polimi.ingsw.server.Logger;
import it.polimi.ingsw.server.ServerConfigFile;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extending the class Game, provides methods with control functions.
 * A GameRoom controls the flow of a Game, providing means to a GameFlowHandler.
 */
public class GameRoom extends Game{
    private List<ClientInterface> connections;
    private boolean notifyEndGame;
    private Timer timer;

    /**
     * Creates a Game Room.
     * @param playerList List of players in the game.
     * @param connections List of connections to which notify.
     * @throws NoMorePlayersException couldn't start a round.
     */
    GameRoom(List<Player> playerList, List<ClientInterface> connections) throws NoMorePlayersException {
        super(playerList);
        this.connections = connections;
        this.notifyEndGame = true;
    }

    /**
     * Notify the usage of a tool card to each player of the game.
     * @param nickname the name of the user that used the tool card.
     * @param toolcard the name of the tool card used.
     * @param window the new window of the player.
     */
    synchronized void notifyAllToolCardUsed(String nickname, String toolcard, Window window){
        connections.forEach(user -> {
            try {
                user.notifyToolCardUse(nickname, toolcard, window, getCurrentRound().getDraftPool(), getRoundTrack());
            } catch (RemoteException e) {
                Logger.print("Disconnection RMI on notify " + e.getMessage());
            }
        });
    }

    /**
     * Notify the usage of a dice to each player of the game.
     * @param nickname the name of the user that placed the dice.
     * @param row the row where the dice has been placed.
     * @param column the column where the dice has been placed.
     * @param dice the dice that has been placed.
     */
    synchronized void notifyAllDicePlaced(String nickname, int row, int column, Dice dice){
        connections.forEach(user -> {
            try {
                user.notifyDicePlaced(nickname, row, column, dice);
            } catch (RemoteException e) {
                Logger.print("Disconnection RMI on notify " + e.getMessage());
            }
        });
    }

    /**
     * Notify the usage of a dice to each player of the game.
     * @param newRound true if the round is a new round, false if it's just a turn.
     */
    private void notifyRound(boolean newRound){
        String firstPlayer = getCurrentRound().getCurrentPlayer().getNickname();
        List<Dice> draftPool = getCurrentRound().getDraftPool();
        List<Dice> roundT = null;

        if (newRound)
            roundT = getRoundTrack();

        final List<Dice> roundTrack = roundT;

        connections.forEach(user -> {
            try {
                user.notifyRound(firstPlayer, draftPool, newRound, roundTrack);
            } catch (RemoteException e) {
                Logger.print("Disconnection RMI on notify " + e.getMessage());
            }
        });
    }

    /**
     * Method used to go on with the game after the game is ready.
     * Cancels timers and gets info about the next turn/round.
     * Check if game is finished.
     */
    synchronized void goOn(){
        if (timer != null)
            timer.cancel();
        boolean newRound = false;
        try {
            getCurrentRound().nextPlayer();
        } catch (NoMorePlayersException e) {
            nextRound();
            newRound = true;
        }
        if (!checkGameFinished() && super.isGameStarted()) {
            notifyRound(newRound);
            startTimer();
        }
    }

    /**
     * Check whether the game is finished or not.
     * Notify game over if necessary.
     * @return true if game finished, false otherwise.
     */
    private synchronized boolean checkGameFinished(){
        if (!isGameFinished()) return false;
        if (notifyEndGame){
            List<Score> scoresList = super.isGameStarted() ? computeFinalScores() : new ArrayList<>();

            //if player is alone, return only his score
            if(getPlayers().stream().filter(p -> !p.isSuspended()).count() == 1)
                scoresList = scoresList.stream().filter(score -> score.getPlayer().equalsIgnoreCase(getPlayers().stream().filter(p -> !p.isSuspended()).findFirst().get().getNickname())).collect(Collectors.toList());

            final List<Score> scores = scoresList;
            connections.forEach(user -> {
                try {
                    user.notifyEndGame(scores);
                } catch (RemoteException e) {
                    Logger.print("Disconnection RMI on notify " + e.getMessage());
                }
            });

            notifyEndGame = false;
            Logger.print("Game Over: " + getPlayers().stream().map(Player::getNickname).collect(Collectors.toList()));
        }
        return true;
    }

    /**
     * Starts the game notifying each player the schemas other players chose and the info about the new round.
     * Sets a timer for the first player.
     */
    synchronized void gameReady(){
        if (!getPlaying()) {
            String firstPlayer = getCurrentRound().getCurrentPlayer().getNickname();
            List<Dice> draftPool = getCurrentRound().getDraftPool();
            HashMap<String, Schema> playersSchemas = new HashMap<>();

            for (Player player : getPlayers())
                playersSchemas.put(player.getNickname(), player.getWindow().getSchema());
            connections.forEach(user -> {
                try {
                    user.notifyOthersSchemas(playersSchemas);
                } catch (RemoteException e) {
                    Logger.print("Disconnection RMI on notify " + e.getMessage());
                }
            });
            connections.forEach(user -> {
                try {
                    user.notifyRound(firstPlayer, draftPool, true, getRoundTrack());
                } catch (RemoteException e) {
                    Logger.print("Disconnection RMI on notify " + e.getMessage());
                }
            });

            setPlaying(true);
            startTimer();
        }
    }

    /**
     * This method allows to replace a connection with another. E.g. in case of a reconnection.
     * @param oldConnection connection to be removed.
     * @param newConnection connection to be saved.
     */
    synchronized void replaceConnection(ClientInterface oldConnection, ClientInterface newConnection){
        this.connections.remove(oldConnection);
        this.connections.add(newConnection);
    }

    /**
     * Logs out a player and notify the others.
     * @param nickname player that is logging out.
     * @param connection connection to be removed.
     */
    synchronized void logout(String nickname, ClientInterface connection){

        connections.remove(connection);
        connections.forEach(conn -> {
            try {
                conn.notifyLogout(nickname);
            } catch (RemoteException e) {
                Logger.print("Disconnection RMI on notify " + e.getMessage());
            }
        });
        suspendPlayer(nickname);
        if (nickname.equalsIgnoreCase(getCurrentRound().getCurrentPlayer().getNickname())) goOn();
        else checkGameFinished();
    }

    /**
     * Returns the list of players' names.
     * @return list of players' names.
     */
    synchronized List<String> getPlayersNick(){
        return super.getPlayers().stream().map(Player::getNickname).collect(Collectors.toList());
    }

    /**
     * Timer Class.
     */
    class TimerExpired extends TimerTask {
        public void run() {
            suspendCurrentPlayer(); goOn();
        }
    }

    /**
     * Sets the timer for timeout during a turn.
     * To be cancelled on GoOn.
     */
    private void startTimer(){
        timer = new Timer();
        timer.schedule(new GameRoom.TimerExpired(), (long) ServerConfigFile.getSecondsTimerTurn() * 1000);
    }

    @Override
    public synchronized void suspendPlayer(String nickname){
        super.suspendPlayer(nickname);
        connections.forEach(conn -> {
            try {
                conn.notifySuspension(nickname);
            } catch (RemoteException e) {
                Logger.print("Disconnection RMI on notify " + e.getMessage());
            }
        });
    }

    @Override
    public synchronized void suspendCurrentPlayer(){
        String nickname = getCurrentRound().getCurrentPlayer().getNickname();
        super.suspendCurrentPlayer();
        connections.forEach(conn -> {
            try {
                conn.notifySuspension(nickname);
            } catch (RemoteException e) {
                Logger.print("Disconnection RMI on notify " + e.getMessage());
            }
        });
    }
}
