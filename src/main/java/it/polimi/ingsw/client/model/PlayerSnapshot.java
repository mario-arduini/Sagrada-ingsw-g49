package it.polimi.ingsw.client.model;

import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Window;

public class PlayerSnapshot {
    private final String nickname;
    private Window window;
    private int favorToken;
    private boolean suspended;
    private String privateGoal;
    private boolean myTurn;
    private boolean diceExtracted;
    private boolean usedToolCard;

    public PlayerSnapshot(String nickname){
        this.nickname = nickname;
        this.favorToken = 0;
        this.suspended = false;
        myTurn = false;
        diceExtracted = false;
        usedToolCard = false;
    }

    PlayerSnapshot(PlayerSnapshot player){
        this.nickname = player.nickname;
        this.window = new Window(player.window);
        this.favorToken = player.favorToken;
        this.suspended = player.suspended;
    }

    public String getNickname() {
        return nickname;
    }

    public Window getWindow() {
        return window;
    }

    public void setWindow(Schema schema){
        this.window = new Window(schema);
        setFavorToken();
    }

    public void setWindow(Window window){
        this.window = window;
    }

    public int getFavorToken() {
        return favorToken;
    }

    public void setFavorToken(int favorToken) {
        this.favorToken = favorToken;
    }

    public void useFavorToken(int number) {
        this.favorToken -= number;
    }

    private void setFavorToken(){
        this.favorToken = window.getSchema().getDifficulty();
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void suspend(){
        this.suspended = true;
    }

    public String getPrivateGoal(){
        return  privateGoal;
    }

    public void setPrivateGoal(String privateGoal){
        this.privateGoal = privateGoal;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public boolean isDiceAlreadyExtracted() {
        return diceExtracted;
    }

    public void setDiceExtracted(boolean diceExtracted) {
        this.diceExtracted = diceExtracted;
    }

    public boolean isToolCardAlreadyUsed() {
        return usedToolCard;
    }

    public void setUsedToolCard(boolean usedToolCard) {
        this.usedToolCard = usedToolCard;
    }

    @Override
    public boolean equals(Object obj){
        if (! (obj instanceof PlayerSnapshot))
            return false;
        PlayerSnapshot player = (PlayerSnapshot) obj;
        if (this.window == null && player.window != null
                || this.window != null && player.window == null)
            return false;
        if (this.window != null && !this.window.equals(player.window))
            return false;

        return this.nickname.equals(player.nickname)
                && this.favorToken == player.favorToken
                && this.suspended == player.suspended;
    }
}
