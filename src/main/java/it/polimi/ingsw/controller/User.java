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
