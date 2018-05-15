package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PlayerAlreadyAddedException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WaitingRoom {
    private static final String TIMEOUT_FILE_NAME = "src/main/resources/timeout.txt";
    private static WaitingRoom waitingRoom;
    private ArrayList<Player> players;
    private boolean timerRunnig;
    private int secondsTimer;
    private Timer timer;

    //reference to obj that contains the list of games currently running
    //when it's time to create a new game startgame() will call a method of that obj passing the new game

    private WaitingRoom(){
        players = new ArrayList<>();
        timerRunnig = false;

        BufferedReader reader = null;
        String s = (new File("./")).getAbsolutePath();
        s = s.substring(0, s.length() - 1) + TIMEOUT_FILE_NAME;
        //s = this.getClass().getResource("timeout.txt").getPath();
        try {
            reader = new BufferedReader(new FileReader(new File(s)));
            String text;

            if ((text = reader.readLine()) != null)
                secondsTimer = Integer.parseInt(text);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            timerRunnig = true;
        }
        else if(players.size() >= 4)
            startGame();
    }

    public void removePlayer(Player player){
        players.remove(player);
        if(players.size() < 2) {
            timer.cancel();
            timerRunnig = false;
        }
    }

/*    public Timer getTimer(){
        return timer;
    }*/

    public boolean isTimerRunning() {
        return timerRunnig;
    }

    private void startGame() {
        Game game = new Game(players);
        players.clear();
        timer.cancel();
        timerRunnig = false;
        //controller.addGame(game);  //TODO fix this: Game needs to be returned somehow (look commet above)
    }

    class TimerExpired extends TimerTask {
        public void run() {
            startGame();
        }
    }
}
