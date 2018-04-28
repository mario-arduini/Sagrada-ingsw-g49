package it.polimi.ingsw.model;

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
        this.favorToken = -1;
        this.suspended = false;
    }

    public void setWindow(Schema schema){

        this.window = new Window(schema);
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

    protected void setPrivateGoal(PrivateGoal privateGoal){

        this.privateGoal = privateGoal;
    }

    protected void useFavorToken(int number) throws NotEnoughFavorTokenException {

        if(this.favorToken >= number)
            this.favorToken -= number;
        else
            throw new NotEnoughFavorTokenException();
    }


}
