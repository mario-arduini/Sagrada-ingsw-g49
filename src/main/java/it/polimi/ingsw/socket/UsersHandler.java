package it.polimi.ingsw.socket;

import it.polimi.ingsw.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class UsersHandler {
    private List<Player> players;
    private static final String ALPHABETH = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int N = ALPHABETH.length();
    private static Random rand = new Random();

    public UsersHandler(){
        this.players = new ArrayList<>();
    }

    public synchronized String login(String nickname){
        String token;
        Optional<Player> playerFetched = findPlayer(nickname);
        if (playerFetched.isPresent())
            return null;
        token = tokenGenerator();
        players.add(new Player(nickname, token));
        return token;
    }

    public synchronized boolean loginLost(String nickname, String token) {
        Optional<Player> playerFetched = findPlayer(nickname);
        return playerFetched.isPresent() && playerFetched.get().verifyAuthToken(token);
    }

    private String tokenGenerator(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20; i++) builder.append((ALPHABETH.charAt(rand.nextInt(N))));
        return builder.toString();
    }

    private Optional<Player> findPlayer(String nickname){
        return players.stream().filter(player -> player.getNickname().equalsIgnoreCase(nickname)).findFirst();
    }
}
