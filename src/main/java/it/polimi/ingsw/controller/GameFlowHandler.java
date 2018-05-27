package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.network.server.ConnectionHandler;
import it.polimi.ingsw.network.server.SocketHandler;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameFlowHandler {
    private User player;
    private Game game;
    private GamesHandler gamesHandler;
    private static Random rand = new Random();
    private static final String ALPHABETH = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int N = ALPHABETH.length();



    public GameFlowHandler(GamesHandler gamesHandler){
        this.player = null;
        this.game = null;
        this.gamesHandler = gamesHandler;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void disconnected(){
        if (game == null){
            gamesHandler.waitingRoomDisconnection(player);
        }
    }

    public List<String> getPlayers(){
        return game == null ? gamesHandler.getWaitingPlayers() : game.getPlayers().stream().map(Player::getNickname).collect(Collectors.toList());
    }

    public String login(String nickname, ConnectionHandler connection) {
        String token;
        token = tokenGenerator();

        this.player = gamesHandler.login(nickname, connection, token);
        return this.player == null ? null : token;
    }

    public void logout() {
        gamesHandler.logout(this.player.getNickname());
    }

    public boolean reconnection(String nickname, SocketHandler socketHandler, String token) {
        this.player = this.gamesHandler.reconnection(nickname,socketHandler,token);
        return this.player != null;
    }

    private String tokenGenerator(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20; i++) builder.append((ALPHABETH.charAt(rand.nextInt(N))));
        return builder.toString();
    }
}
