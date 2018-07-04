package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidFavorTokenNumberException;
import it.polimi.ingsw.model.exceptions.NotEnoughFavorTokenException;
import it.polimi.ingsw.model.exceptions.PrivateGoalAlreadySetException;
import it.polimi.ingsw.model.exceptions.WindowAlreadySetException;
import it.polimi.ingsw.model.goalcards.PrivateGoal;

public class Player {
    private final String nickname;
    private String authToken;
    private PrivateGoal privateGoal;
    private Window window;
    private int favorToken;
    private boolean suspended;

    public Player(String nickname,String authToken){
        this.nickname = nickname;
        this.authToken = authToken;
        this.privateGoal = null;
        this.window = null;
        this.favorToken = 0;
        this.suspended = false;
    }

    public Player(Player player){
        this.nickname = player.nickname;
        this.authToken = player.authToken;
        this.privateGoal = null;
        this.window = null;
        this.favorToken = 0;
        this.suspended = false;
    }

    @Override
    public boolean equals(Object obj){
        if (! (obj instanceof Player))
            return false;
        Player player = (Player) obj;
        if (this.privateGoal == null && player.privateGoal != null
                || this.privateGoal != null && player.privateGoal == null
                || this.window == null && player.window != null
                || this.window != null && player.window == null)
            return false;
        if (this.privateGoal != null && !this.privateGoal.equals(player.privateGoal)
                || this.window != null && !this.window.equals(player.window))
            return false;

        return this.nickname.equals(player.nickname)
                && this.authToken.equals(player.authToken)
                && this.favorToken == player.favorToken
                && this.suspended == player.suspended;
    }

    public void setWindow(Schema schema) throws WindowAlreadySetException {
        if (this.window != null)
            throw new WindowAlreadySetException();
        this.window = new Window(schema);
        setFavorToken();
    }

    public void setWindow(Window window) {
        this.window = window;

    }

    public String getNickname() {

        return nickname;
    }

    public Window getWindow() {

        return window;
    }

    public PrivateGoal getPrivateGoal() {

        return privateGoal;
    }

    public int getFavorToken() {

        return favorToken;
    }

    public boolean isSuspended() {

        return suspended;
    }

    public void suspend(){
        this.suspended = true;
    }

    public boolean verifyAuthToken(String authToken){

        return this.authToken.equals(authToken);
    }

    public void setPrivateGoal(PrivateGoal privateGoal) throws PrivateGoalAlreadySetException{
        if (privateGoal == null)
            throw new PrivateGoalAlreadySetException();
        this.privateGoal = privateGoal;
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

}
