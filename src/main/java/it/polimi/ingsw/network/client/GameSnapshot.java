package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.Dice;
import it.polimi.ingsw.network.client.Schema;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.network.client.ToolCard;

import java.util.*;

public class GameSnapshot {
    private PlayerSnapshot player;
    private final List<PlayerSnapshot> otherPlayers;
    private List<Dice> roundTrack;
    private int trackIndex;
    private List<ToolCard> toolCards;
    private List<PublicGoal> publicGoals;
    private int nextFirstPlayer;
    private List<Dice> draftPool;
    private static final int schemaPerPlayer = 4;

    public GameSnapshot(String playerNick) { //Fix UML for players
        this.roundTrack = new ArrayList<>();
        this.toolCards = new ArrayList<>();
        this.trackIndex = 0;
        this.publicGoals = new ArrayList<>();
        this.otherPlayers = new ArrayList<>();
        this.player = new PlayerSnapshot(playerNick);
        this.draftPool = new ArrayList<>();
    }

    public PlayerSnapshot getPlayer() {
        return player;
    }

    public void addOtherPlayer(String nick,Schema schema){
        PlayerSnapshot newPlayer = new PlayerSnapshot(nick);
        newPlayer.setWindow(schema);
        otherPlayers.add(newPlayer);
    }

    public List<PlayerSnapshot> getOtherPlayers() {
        return otherPlayers;
    }

    public List<ToolCard> getToolCards() {
        return toolCards;
    }

    public ToolCard getToolCardByName(String name) {

        for(ToolCard toolCard : toolCards)
            if(toolCard.getName().equals(name))
                return toolCard;
        return null;
    }

    public void setPublicGoals(List<PublicGoal> publicGoals) {
        this.publicGoals = publicGoals;
    }

    public List<PublicGoal> getPublicGoals() {
        return publicGoals;
    }

    public List<Dice> getRoundTrack() {
        return roundTrack;
    }

    public List<Dice> getDraftPool(){
        return draftPool;
    }

    public void setDraftPool(List<Dice> draftPool){
        this.draftPool = new ArrayList<>(draftPool);
    }

    public void setToolCards(List<ToolCard> extractedToolCards){
        toolCards = extractedToolCards;
    }

    public Optional<PlayerSnapshot> findPlayer(String nickname){
        List <PlayerSnapshot> allPlayers = new ArrayList<>();
        allPlayers.add(this.player);
        allPlayers.addAll(this.otherPlayers);
        return allPlayers.stream().filter(player -> player.getNickname().equalsIgnoreCase(nickname)).findFirst();
    }




}