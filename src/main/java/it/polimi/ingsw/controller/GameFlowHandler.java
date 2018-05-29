package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.network.server.ConnectionHandler;

import java.util.List;
import java.util.stream.Collectors;

public class GameFlowHandler {
    private User player;
    private Game game;
    private GamesHandler gamesHandler;
    private List<Schema> initialSchemas;

    public GameFlowHandler(GamesHandler gamesHandler){
        this.player = null;
        this.game = null;
        this.gamesHandler = gamesHandler;
    }

    public void setGame(Game game) {
        this.game = game;
        initialSchemas = game.extractSchemas();
        player.notifySchemas(initialSchemas);
    }

    public void chooseSchema(Integer schemaNumber){
        player.setWindow(initialSchemas.get(schemaNumber));
    }

    public void checkGameReady(){
        List<Player> inGamePlayers = game.getPlayers();
        for (Player player: inGamePlayers)
            if (player.getWindow()==null)
                return;
        gamesHandler.gameReady(game);
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
