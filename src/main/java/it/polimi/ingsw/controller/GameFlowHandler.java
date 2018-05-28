package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.network.server.ConnectionHandler;

import java.util.List;
import java.util.stream.Collectors;

public class GameFlowHandler {
    private User player;
    private Game game;
    private GamesHandler gamesHandler;

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

    public boolean login(String nickname, String password, ConnectionHandler connection) {
        this.player = gamesHandler.login(nickname, password, connection);
        return this.player != null;
    }

    public void logout() {
        gamesHandler.logout(this.player.getNickname());
    }
}
