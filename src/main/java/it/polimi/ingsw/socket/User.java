package it.polimi.ingsw.socket;

import it.polimi.ingsw.model.Player;

import java.util.List;

public class User extends Player {
    ConnectionHandler connection;


    public User(String nickname, String authToken, ConnectionHandler connection) {
        super(nickname, authToken);
        this.connection = connection;
    }

    public void setConnection(ConnectionHandler connection) {
        this.connection = connection;
    }

    public void notifyUsers(String nickname) {
        this.connection.notifyPlayers(nickname);
    }

    public void notifyUsers(List<String> nicknames) {
        this.connection.notifyPlayers(nicknames);
    }
}
