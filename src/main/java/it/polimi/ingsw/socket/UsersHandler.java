package it.polimi.ingsw.socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class UsersHandler {
    private List<User> players;
    private static final String ALPHABETH = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int N = ALPHABETH.length();
    private static Random rand = new Random();

    public UsersHandler(){
        this.players = new ArrayList<>();
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
        return token;
    }

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
    }

}
