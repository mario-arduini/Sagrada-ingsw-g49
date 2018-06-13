package it.polimi.ingsw.network.client.model;

import it.polimi.ingsw.network.client.model.exception.InvalidFavorTokenNumberException;
import it.polimi.ingsw.network.client.model.exception.NotEnoughFavorTokenException;

public class PlayerSnapshot {
    private final String nickname;
    private Window window;
    private int favorToken;
    private boolean suspended;
    private PrivateGoal privateGoal;
    private boolean myTurn;
    private boolean diceExtracted;
    private boolean usedToolCard;

    PlayerSnapshot(String nickname){
        this.nickname = nickname;
        this.favorToken = 0;
        this.suspended = false;
        myTurn = false;
        diceExtracted = false;
        usedToolCard = false;
    }

    public PlayerSnapshot(PlayerSnapshot player){
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
    }   //TODO: possibly remove

    public int getFavorToken() {
        return favorToken;
    }

    public void useFavorToken(int number) throws NotEnoughFavorTokenException, InvalidFavorTokenNumberException {
        if (number < 0) throw new InvalidFavorTokenNumberException();
        if(this.favorToken >= number)
            this.favorToken -= number;
        else
            throw new NotEnoughFavorTokenException();
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

    public PrivateGoal getPrivateGoal(){
        return  privateGoal;
    }

    public void setPrivateGoal(PrivateGoal privateGoal){
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
