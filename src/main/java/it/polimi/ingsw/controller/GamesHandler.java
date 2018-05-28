package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.network.server.ConnectionHandler;
import it.polimi.ingsw.network.server.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GamesHandler {
    private List<User> players;
    private HashMap<User,Game> gameReference;
    private static final String TIMEOUT_FILE_NAME = "src/main/resources/timeout.txt";
    private int secondsTimer;
    private Timer timer;


    public GamesHandler(){
        this.players = new ArrayList<>();
        this.gameReference = new HashMap<>();
        String filename = (new File("./")).getAbsolutePath();
        filename = filename.substring(0, filename.length() - 1) + TIMEOUT_FILE_NAME;
        this.secondsTimer = readIntFromFile(filename);
        //TODO: Throw exception if file does not exist
    }

    public synchronized User login(String nickname, ConnectionHandler connection, String token){
        User user;
        Optional<User> playerFetched = findPlayer(nickname);

        if (playerFetched.isPresent())
            return null;

        players.forEach(p -> p.notifyLogin(nickname));
        user = new User(nickname, token, connection);
        user.notifyLogin(getPlayerNicks());
        Logger.print("Logged in: " + nickname + " " + connection.getRemoteAddress());
        players.add(user);
        waitingRoomNewPlayer();
        return user;
    }

    public synchronized User reconnection(String nickname,  ConnectionHandler connection, String token) {
        Optional<User> playerFetched = findPlayer(nickname);
        User user;
        if (playerFetched.isPresent()){
            user = playerFetched.get();
            if (gameReference.containsKey(user) && user.verifyAuthToken(token)){
                user.setConnection(connection);
                user.setGame(gameReference.get(user));
                Logger.print("Reconnected: " + nickname + " " + connection.getRemoteAddress());
                List<String> users = getPlayerNicks();
                List<String> loggedUsers = users.stream().filter(nick -> !nick.equalsIgnoreCase(nickname)).collect(Collectors.toList());

                user.notifyLogin(loggedUsers);
                return user;
            }
        }
        return null;
    }

    private Optional<User> findPlayer(String nickname){
        List <User> allPlayers = new ArrayList<>();
        allPlayers.addAll(this.players);
        allPlayers.addAll(this.gameReference.keySet());
        return allPlayers.stream().filter(player -> player.getNickname().equalsIgnoreCase(nickname)).findFirst();
    }

    private List<String> getPlayerNicks(){
        return players.stream().map(player -> player.getNickname()).collect(Collectors.toList());
    }

    public synchronized void logout(String nickname){
        Optional<User> playerFetched = findPlayer(nickname);
        User user = playerFetched.get();
        if (players.contains(user)) {
            players.remove(user);
            players.forEach(p -> p.notifyLogout(nickname));
            waitingRoomLeftPlayer(user);
        }else{
            Game game = gameReference.get(user);
            gameReference.remove(user);
            List<User> cooplayers = getPlayersByGame(game);
            cooplayers.forEach(p -> p.notifyLogout(nickname));
        }
        Logger.print("Logged out: " + user.getNickname() + " " + user.getConnection().getRemoteAddress());
    }

    private int readIntFromFile(String filename){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(filename)));
            String text;

            if ((text = reader.readLine()) != null)
                return Integer.parseInt(text);

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
        return -1;
    }

    private synchronized void waitingRoomNewPlayer(){
        if(players.size() == 2) {
            timer = new Timer();
            timer.schedule(new GamesHandler.TimerExpired(), (long) secondsTimer * 1000);
        }
        else if(players.size() >= 4)
            startGame();
    }

    private synchronized void waitingRoomLeftPlayer(User player){
        players.remove(player);
        if(players.size() < 2 && timer != null) {
            timer.cancel();
        }
    }

    public synchronized void waitingRoomDisconnection(User player){
        players.remove(player);
    }

    private synchronized void startGame() {
        List<Player> playerList = new ArrayList<>(players);
        Game game = new Game(playerList);

        for (User player:players) {
            gameReference.put(player, game);
            player.setGame(game);
        }

        Logger.print(String.format("Game Started: %s", getPlayerNicks().toString()));
        players.clear();
        if (timer != null)
            timer.cancel();
    }

    public List<String> getWaitingPlayers() {
        return getPlayerNicks();
    }

    class TimerExpired extends TimerTask {
        public void run() {
            startGame();
        }
    }

    public List<User> getPlayersByGame(Game game) {
        List<User> keys = new ArrayList<>();
        for (Map.Entry<User, Game> entry : gameReference.entrySet()) {
            if (Objects.equals(game, entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

}
