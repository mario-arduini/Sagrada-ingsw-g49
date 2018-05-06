package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidFavorTokenNumberException;
import it.polimi.ingsw.model.exceptions.NotEnoughFavorTokenException;

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

    public void setWindow(Schema schema){
        this.window = new Window(schema);
        setFavorToken();
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

    public boolean verifyAuthToken(String authToken){

        return this.authToken.equals(authToken);
    }

    //Protected?
    public void setPrivateGoal(PrivateGoal privateGoal){

        this.privateGoal = privateGoal;
    }

    //Protected?
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
