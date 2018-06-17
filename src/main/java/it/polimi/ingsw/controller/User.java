package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.goalcards.PrivateGoal;
import it.polimi.ingsw.model.goalcards.PublicGoal;
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

    public void setGame(GameRoom game){
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

    public void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack){
        connection.notifyRound(currentPlayer, draftPool, newRound, roundTrack);
    }

    public void notifyOthersSchemas(HashMap<String, Schema> playersSchemas){
        connection.notifyOthersSchemas(playersSchemas);
    }

    public void notifyDicePlaced(String nickname, int row, int column, Dice dice){
        connection.notifyDicePlaced(nickname, row, column, dice);
    }

    public void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack){
        this.connection.notifyToolCardUse(player, toolCard, window, draftPool, roundTrack);
    }

    public void notifyGameInfo(List<ToolCard> toolCards, List<PublicGoal> publicGoals, PrivateGoal privateGoal){
        connection.notifyGameInfo(toolCards, publicGoals, privateGoal);
    }

    public void notifyReconInfo(HashMap<String, Window> windows, HashMap<String, Integer> favorToken,List<Dice> roundTrack){
        connection.notifyReconInfo(windows, favorToken, roundTrack);
    }

    public Coordinate askDiceWindow(String prompt){
        return connection.askDiceWindow(prompt);
    }

    public Dice askDiceDraftPool(String prompt){
        return connection.askDiceDraftPool(prompt);
    }

    public int askDiceRoundTrack(String prompt){
        return connection.askDiceRoundTrack(prompt);
    }

    public boolean askIfPlus(String prompt) {
        return connection.askIfPlus(prompt);
    }

    public int askDiceValue(String prompt){
        return connection.askDiceValue(prompt);
    }

}
