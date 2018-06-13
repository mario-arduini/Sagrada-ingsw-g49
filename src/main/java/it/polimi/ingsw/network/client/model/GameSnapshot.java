package it.polimi.ingsw.network.client.model;

import java.util.*;

public class GameSnapshot {
    private PlayerSnapshot player;
    private final List<PlayerSnapshot> otherPlayers;
    private List<Dice> roundTrack;
    private List<ToolCard> toolCards;
    private List<String> publicGoals;
    private List<Dice> draftPool;

    public GameSnapshot() {
        this.roundTrack = new ArrayList<>();
        this.toolCards = new ArrayList<>();
        this.publicGoals = new ArrayList<>();
        this.otherPlayers = new ArrayList<>();
        this.draftPool = new ArrayList<>();
    }

    public PlayerSnapshot getPlayer() {
        return player;
    }

    public void setPlayer(String nickname) {
        this.player = new PlayerSnapshot(nickname);
    }

    //region DEPRECATED
//    public void addOtherPlayer(String nick, Schema schema){
//        PlayerSnapshot newPlayer = new PlayerSnapshot(nick);
//        newPlayer.setWindow(schema);
//        otherPlayers.add(newPlayer);
//    }
    //endregion

    public void addOtherPlayer(String nick){
        otherPlayers.add(new PlayerSnapshot(nick));
    }

    public void removeOtherPlayer(String nick){
        for(PlayerSnapshot user : otherPlayers)
            if(user.getNickname().equals(nick))
                otherPlayers.remove(user);
    }

    public List<PlayerSnapshot> getOtherPlayers() {
        return otherPlayers;
    }

    public Optional<PlayerSnapshot> findPlayer(String nickname){
        List <PlayerSnapshot> allPlayers = new ArrayList<>();
        allPlayers.add(this.player);
        allPlayers.addAll(this.otherPlayers);
        return allPlayers.stream().filter(user -> user.getNickname().equalsIgnoreCase(nickname)).findFirst();
    }

    public List<Dice> getRoundTrack() {
        return roundTrack;
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

    public void setToolCards(List<ToolCard> extractedToolCards){
        toolCards = extractedToolCards;
    }

    public List<String> getPublicGoals() {
        return publicGoals;
    }

    public void setPublicGoals(List<String> publicGoals) {
        this.publicGoals = publicGoals;
    }

    public List<Dice> getDraftPool(){
        return draftPool;
    }

    public void setDraftPool(List<Dice> draftPool){
        this.draftPool = new ArrayList<>(draftPool);
    }
}