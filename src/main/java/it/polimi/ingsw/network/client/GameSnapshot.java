package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.goalcards.PublicGoal;
import java.util.*;

public class GameSnapshot {
    private PlayerSnapshot player;
    private final List<PlayerSnapshot> otherPlayers;
    private List<Dice> roundTrack;
    private List<ToolCard> toolCards;
    private List<PublicGoal> publicGoals;
    private List<Dice> draftPool;

    GameSnapshot(String playerNick) {
        this.roundTrack = new ArrayList<>();
        this.toolCards = new ArrayList<>();
        this.publicGoals = new ArrayList<>();
        this.otherPlayers = new ArrayList<>();
        this.player = new PlayerSnapshot(playerNick);
        this.draftPool = new ArrayList<>();
    }

    public PlayerSnapshot getPlayer() {
        return player;
    }

    void addOtherPlayer(String nick,Schema schema){
        PlayerSnapshot newPlayer = new PlayerSnapshot(nick);
        newPlayer.setWindow(schema);
        otherPlayers.add(newPlayer);
    }

    List<PlayerSnapshot> getOtherPlayers() {
        return otherPlayers;
    }

    List<ToolCard> getToolCards() {
        return toolCards;
    }

    ToolCard getToolCardByName(String name) {

        for(ToolCard toolCard : toolCards)
            if(toolCard.getName().equals(name))
                return toolCard;
        return null;
    }

    void setPublicGoals(List<PublicGoal> publicGoals) {
        this.publicGoals = publicGoals;
    }

    List<PublicGoal> getPublicGoals() {
        return publicGoals;
    }

    List<Dice> getRoundTrack() {
        return roundTrack;
    }

    List<Dice> getDraftPool(){
        return draftPool;
    }

    void setDraftPool(List<Dice> draftPool){
        this.draftPool = new ArrayList<>(draftPool);
    }

    void setToolCards(List<ToolCard> extractedToolCards){
        toolCards = extractedToolCards;
    }

    Optional<PlayerSnapshot> findPlayer(String nickname){
        List <PlayerSnapshot> allPlayers = new ArrayList<>();
        allPlayers.add(this.player);
        allPlayers.addAll(this.otherPlayers);
        return allPlayers.stream().filter(user -> user.getNickname().equalsIgnoreCase(nickname)).findFirst();
    }




}