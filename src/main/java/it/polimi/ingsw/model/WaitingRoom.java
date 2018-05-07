package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PlayerAlreadyAddedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WaitingRoom {
    private static WaitingRoom waitingRoom;
    private ArrayList<Player> players;
    private int secondsTimer;
    private Timer timer;

    private WaitingRoom(){
        players = new ArrayList<Player>();
        //TODO: LOAD secondsTime FROM FILE
    }

    public static WaitingRoom getWaitingRoom(){
        if(waitingRoom == null)
            waitingRoom = new WaitingRoom();
        return waitingRoom;
    }

    public List<Player> getPlayers(){
        return players;
    }

    public void addPlayer(Player newPlayer) throws PlayerAlreadyAddedException {
        for(Player player: players)
            if(newPlayer.getNickname().equals(player.getNickname()))
                throw new PlayerAlreadyAddedException();
        players.add(newPlayer);
        if(players.size() == 2) {
            timer = new Timer();
            timer.schedule(new TimerExpired(), (long) secondsTimer * 1000);
        }
        else if(players.size() >= 4)
            startGame();
    }

    public void removePlayer(Player player){
        players.remove(player);
        if(players.size() < 2)
            timer.cancel();
    }

    public Timer getTimer(){
        return timer;
    }

    private Game startGame(){
        Game game = new Game(players);
        players.clear();
        timer.cancel();
        return game;
    }

    class TimerExpired extends TimerTask {
        public void run() {
            startGame();
        }
    }
}
