package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.network.server.ConnectionHandler;

import java.util.List;

public class User extends Player {
    private ConnectionHandler connection;

    public User(String nickname, String authToken, ConnectionHandler connection) {
        super(nickname, authToken);
        this.connection = connection;
    }

    public ConnectionHandler getConnection(){
        return this.connection;
    }

    public void setConnection(ConnectionHandler connection){
        this.connection.close();
        this.connection = connection;
    }

    public void setConnection(ConnectionHandler connection, Game game) {
        this.connection.close();
        this.connection = connection;
        this.connection.setGame(game);
    }

    public void setGame(Game game){
        this.connection.setGame(game);
    }

    public void notifyLogin(String nickname) {
        this.connection.notifyLogin(nickname);
    }

    public void notifyLogin(List<String> nicknames) {
        this.connection.notifyLogin(nicknames);
    }

    public void notifyLogout(String nickname) {
        this.connection.notifyLogout(nickname);
    }

    public void notifySchemas(){
    }

}
