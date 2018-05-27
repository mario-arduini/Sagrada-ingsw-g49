package it.polimi.ingsw.network.server;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.WaitingRoom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class UsersHandler {
    private List<User> players;
    private HashMap<User,Game> gameReference;
    private static final String ALPHABETH = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int N = ALPHABETH.length();
    private static final String TIMEOUT_FILE_NAME = "src/main/resources/timeout.txt";
    private static Random rand = new Random();
    private int secondsTimer;
    private Timer timer;


    public UsersHandler(){
        this.players = new ArrayList<>();
        this.gameReference = new HashMap<>();
        String filename = (new File("./")).getAbsolutePath();
        filename = filename.substring(0, filename.length() - 1) + TIMEOUT_FILE_NAME;
        this.secondsTimer = readIntFromFile(filename);
        //TODO: Throw exception if file does not exist
    }

    public synchronized String login(String nickname, ConnectionHandler connection){
        String token;
        User user;
        Optional<User> playerFetched = findPlayer(nickname);
        if (playerFetched.isPresent())
            return null;
        token = tokenGenerator();
        players.stream().forEach(p -> p.notifyLogin(nickname));
        user = new User(nickname, token, connection);
        user.notifyLogin(getPlayerNicks());
        Logger.print("Logged in: " + nickname + " " + connection.getRemoteAddress());
        players.add(user);
        waitingRoomNewPlayer();
        return token;
    }

    //TODO: introduce remove from waiting room if disconnection.
    public synchronized boolean login(String nickname,  ConnectionHandler connection, String token) {
        Optional<User> playerFetched = findPlayer(nickname);
        User user;
        if (playerFetched.isPresent()){
            user = playerFetched.get();
            if (user.verifyAuthToken(token)) {
                user.setConnection(connection);
                Logger.print("Reconnected: " + nickname + " " + connection.getRemoteAddress());
                List<String> users = getPlayerNicks();
                List<String> loggedUsers = users.stream().filter(nick -> !nick.equalsIgnoreCase(nickname)).collect(Collectors.toList());

                user.notifyLogin(loggedUsers);
                return true;
            }
        }
        return false;
    }

    private String tokenGenerator(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20; i++) builder.append((ALPHABETH.charAt(rand.nextInt(N))));
        return builder.toString();
    }

    private Optional<User> findPlayer(String nickname){
        return players.stream().filter(player -> player.getNickname().equalsIgnoreCase(nickname)).findFirst();
    }

    private List<String> getPlayerNicks(){
        return players.stream().map(player -> player.getNickname()).collect(Collectors.toList());
    }

    public synchronized void logout(String nickname){
        Optional<User> playerFetched = findPlayer(nickname);
        User user = playerFetched.get();
        players.remove(user);
        players.stream().forEach(p -> p.notifyLogout(nickname));
        Logger.print("Logged out: " + user.getNickname() + " " + user.getConnection().getRemoteAddress());
        if (players.contains(user))
            waitingRoomLeftPlayer(user);
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
            timer.schedule(new UsersHandler.TimerExpired(), (long) secondsTimer * 1000);
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

    private synchronized void startGame() {
        List<Player> playerList = new ArrayList<>(players);
        Game game = new Game(playerList);

        for (User player:players)
            gameReference.put(player, game);

        Logger.print(String.format("Game Started: %s", getPlayerNicks().toString()));
        players.clear();
        if (timer != null)
            timer.cancel();
    }

    class TimerExpired extends TimerTask {
        public void run() {
            startGame();
        }
    }

}
