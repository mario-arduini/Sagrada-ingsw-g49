package it.polimi.ingsw.controller;

import com.sun.java.swing.plaf.windows.WindowsTreeUI;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.NoMorePlayersException;
import it.polimi.ingsw.network.server.ConnectionHandler;
import it.polimi.ingsw.network.server.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class GameRoom extends Game{
    private List<ConnectionHandler> connections;
    private boolean notifyEndGame;
    private Timer timer;
    private int secondsTimer = 120; //TODO: read value from file.

    GameRoom(List<Player> playerList, List<ConnectionHandler> connections) throws NoMorePlayersException {
        super(playerList);
        this.connections = connections;
        this.notifyEndGame = true;
    }

    public synchronized void notifyAllToolCardUsed(String nickname, String toolcard, Window window){
        connections.forEach(user -> user.notifyToolCardUse(nickname, toolcard, window, getCurrentRound().getDraftPool(), getRoundTrack()));
    }

    public synchronized void notifyAllDicePlaced(String nickname, int row, int column, Dice dice){
        connections.forEach(user -> user.notifyDicePlaced(nickname, row, column, dice));
    }

    public synchronized void goOn(){
        if (timer != null)
            timer.cancel();
        boolean newRound = false;
        try {
            getCurrentRound().nextPlayer();
        } catch (NoMorePlayersException e) {
            nextRound();
            newRound = true;
        }
        if (!isGameFinished()) {
            notifyRound(newRound);
            startTimer();
        }else if(notifyEndGame) {
            connections.forEach(user -> user.notifyEndGame(computeFinalScores()));
            notifyEndGame = false;
            Logger.print("Game Over: " + getPlayers().stream().map(Player::getNickname).collect(Collectors.toList()));
        }
    }

    private void notifyRound(boolean newRound){
        String firstPlayer = getCurrentRound().getCurrentPlayer().getNickname();
        List<Dice> draftPool = getCurrentRound().getDraftPool();

        if (newRound)
            connections.forEach(user -> user.notifyRound(firstPlayer, draftPool, true, getRoundTrack()));
        connections.forEach(user -> user.notifyRound(firstPlayer, draftPool, false, null));
    }

    public synchronized void gameReady(){
        if (!getPlaying()) {
            String firstPlayer = getCurrentRound().getCurrentPlayer().getNickname();
            List<Dice> draftPool = getCurrentRound().getDraftPool();
            HashMap<String, Schema> playersSchemas = new HashMap<>();

            for (Player player : getPlayers())
                playersSchemas.put(player.getNickname(), player.getWindow().getSchema());
            connections.forEach(user -> user.notifyOthersSchemas(playersSchemas));
            connections.forEach(user -> user.notifyRound(firstPlayer, draftPool, true, getRoundTrack()));

            setPlaying(true);
            startTimer();
        }
    }

    private void startTimer(){
        timer = new Timer();
        timer.schedule(new GameRoom.TimerExpired(), (long) secondsTimer * 1000);
    }

    public synchronized void replaceConnection(ConnectionHandler oldConnection, ConnectionHandler newConnection){
        this.connections.remove(oldConnection);
        this.connections.add(newConnection);
    }

    public synchronized void logout(String nickname, ConnectionHandler connection){
        connections.remove(connection);
        connections.forEach(conn -> conn.notifyLogout(nickname));
        suspendPlayer(nickname);
        if (nickname.equalsIgnoreCase(getCurrentRound().getCurrentPlayer().getNickname())) goOn();
    }

    protected synchronized List<String> getPlayersNick(){
        return super.getPlayers().stream().map(Player::getNickname).collect(Collectors.toList());
    }

    class TimerExpired extends TimerTask {
        public void run() {
            suspendCurrentPlayer(); goOn();
        }
    }
}
