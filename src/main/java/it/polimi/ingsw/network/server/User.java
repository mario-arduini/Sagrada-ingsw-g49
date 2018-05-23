package it.polimi.ingsw.network.server;

import it.polimi.ingsw.model.Player;

import java.util.List;

public class User extends Player {
    ConnectionHandler connection;

    public User(String nickname, String authToken, ConnectionHandler connection) {
        super(nickname, authToken);
        this.connection = connection;
    }

    public ConnectionHandler getConnection(){
        return this.connection;
    }

    public void setConnection(ConnectionHandler connection) {
        this.connection.close();
        this.connection = connection;
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

}
