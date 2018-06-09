package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.NoMorePlayersException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameRoom extends Game{
    private List<User> users;

    GameRoom(List<Player> playerList) throws NoMorePlayersException {
        super(playerList);
        users = new ArrayList<>();
        for (Player p: playerList){
            users.add((User) p);
        }
    }

    public synchronized void notifyAllToolCardUsed(String nickname, String toolcard, Window window){
        users.forEach(user -> user.notifyToolCardUse(nickname, toolcard, window));
    }

    public synchronized void notifyAllDicePlaced(String nickname, int row, int column, Dice dice){
        users.forEach(user -> user.notifyDicePlaced(nickname, row, column, dice));
    }

    public synchronized void goOn() {
        boolean newRound = false;
        try {
            getCurrentRound().nextPlayer();
            //Logger.print("Player playing1: " + game.getCurrentRound().getCurrentPlayer().getNickname());
        }catch (NoMorePlayersException e){
            nextRound();
            newRound = true;
            //Logger.print("Player playing2: " + game.getCurrentRound().getCurrentPlayer().getNickname());
        }
        String firstPlayer = getCurrentRound().getCurrentPlayer().getNickname();

        List<Dice> draftPool = getCurrentRound().getDraftPool();

        //TODO: why does functional require this final values here??
        boolean finalNewRound = newRound;
        users.forEach(player -> player.notifyRound(firstPlayer, draftPool, finalNewRound));
    }

    public synchronized void gameReady(){
        if (!getPlaying()) {
            String firstPlayer = getCurrentRound().getCurrentPlayer().getNickname();
            List<Dice> draftPool = getCurrentRound().getDraftPool();
            HashMap<String, Schema> playersSchemas = new HashMap<>();

            for (Player player : users)
                playersSchemas.put(player.getNickname(), player.getWindow().getSchema());
            users.forEach(user -> user.notifyOthersSchemas(playersSchemas));

            users.forEach(user -> user.notifyRound(firstPlayer, draftPool, true));
        }
    }
}
