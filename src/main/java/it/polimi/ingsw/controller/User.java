package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.toolcards.ToolCard;
import it.polimi.ingsw.network.server.ConnectionHandler;

import java.util.HashMap;
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

    public void notifySchemas(List<Schema> schemas){
        this.connection.notifySchemas(schemas);
    }

    public void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound){
        connection.notifyRound(currentPlayer, draftPool, newRound);
    }

    public void notifyToolCards(List<ToolCard> toolCards){
        connection.notifyToolCards(toolCards);
    }

    public void notifyOthersSchemas(HashMap<String, Schema> playersSchemas){
        connection.notifyOthersSchemas(playersSchemas);
    }

    public void notifyDicePlaced(String nickname, int row, int column, Dice dice){
        connection.notifyDicePlaced(nickname, row, column, dice);
    }

    public void notifyToolCardUse(String player, String toolCard){
        this.connection.notifyToolCardUse(player, toolCard);
    }

    public Coordinate askDiceWindow(){
        return connection.askDiceWindow();
    }

    public Dice askDiceDraftPool(){
        return connection.askDiceDraftPool();
    }

    public int askDiceRoundTrack(){
        return connection.askDiceRoundTrack();
    }

    public boolean askIfPlus() {
        return connection.askIfPlus();
    }
}
