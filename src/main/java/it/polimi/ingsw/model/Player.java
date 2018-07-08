package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidFavorTokenNumberException;
import it.polimi.ingsw.model.exceptions.NotEnoughFavorTokenException;
import it.polimi.ingsw.model.exceptions.PrivateGoalAlreadySetException;
import it.polimi.ingsw.model.exceptions.WindowAlreadySetException;
import it.polimi.ingsw.model.goalcards.PrivateGoal;

/**
 * Class representing a Player in the Game
 */
public class Player {
    private final String nickname;
    private String authToken;
    private PrivateGoal privateGoal;
    private Window window;
    private int favorToken;
    private boolean suspended;

    /**
     * Construct Player on connection, using nickname and password given by the user
     * @param nickname Nickname chosen by the user
     * @param authToken Password chosen by the user
     */
    public Player(String nickname,String authToken){
        this.nickname = nickname;
        this.authToken = authToken;
        this.privateGoal = null;
        this.window = null;
        this.favorToken = 0;
        this.suspended = false;
    }

    /**
     * Duplicate a Player
     * @param player Player to duplicate
     */
    public Player(Player player){
        this.nickname = player.nickname;
        this.authToken = player.authToken;
        this.privateGoal = null;
        this.window = null;
        this.favorToken = 0;
        this.suspended = false;
    }

    /**
     * Check if the argument is equal to the caller
     * @param obj Player to compare
     * @return true if players are equal, false otherwise
     */
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

    /**
     * Assign a Schema to the Player if there is none
     * @param schema Schema chosen by the Player
     * @throws WindowAlreadySetException signals Player has already a schema assigned
     */
    public void setWindow(Schema schema) throws WindowAlreadySetException {
        if (this.window != null)
            throw new WindowAlreadySetException();
        this.window = new Window(schema);
        setFavorToken();
    }

    /**
     * Update the Window of the Player with the one given
     * @param window Window given to set
     */
    public void setWindow(Window window) {
        this.window = window;

    }

    /**
     * Get Player's nickname
     * @return Player's nickname
     */
    public String getNickname() {

        return nickname;
    }

    /**
     * Get Player's Window
     * @return Player's Window
     */
    public Window getWindow() {

        return window;
    }

    /**
     * Get Player's Private Goal
     * @return  Player's Private Goal
     */
    public PrivateGoal getPrivateGoal() {

        return privateGoal;
    }

    /**
     * Get Player's numbers of favor tokens
     * @return Player's numbers of favor tokens
     */
    public int getFavorToken() {

        return favorToken;
    }

    /**
     * Check if Player is Suspended
     * @return true if Player is suspended, false otherwise
     */
    public boolean isSuspended() {

        return suspended;
    }

    /**
     * Suspend the Player
     */
    public void suspend(){
        this.suspended = true;
    }

    /**
     * Compare given String with Player's Password
     * @return true if are equals, false otherwise
     */
    public boolean verifyAuthToken(String authToken){

        return this.authToken.equals(authToken);
    }

    /**
     * Set Player Private Goal
     * @param privateGoal Goal to be set
     * @throws PrivateGoalAlreadySetException signals a Private goal is already set
     */
    public void setPrivateGoal(PrivateGoal privateGoal) throws PrivateGoalAlreadySetException{
        if (privateGoal == null)
            throw new PrivateGoalAlreadySetException();
        this.privateGoal = privateGoal;
    }

    /**
     * Diminuish Player's favor tokens by number
     * @param number Favor tokens to use
     * @throws NotEnoughFavorTokenException signals that the Player has not enough favor tokens
     * @throws InvalidFavorTokenNumberException signals that the input was a negative number, therefore not valid
     */
    public void useFavorToken(int number) throws NotEnoughFavorTokenException, InvalidFavorTokenNumberException {
        if (number < 0) throw new InvalidFavorTokenNumberException();
        if(this.favorToken >= number)
            this.favorToken -= number;
        else
            throw new NotEnoughFavorTokenException();
    }

    /**
     * Set the initial number of favor tokens based on the schema chosen
     */
    private void setFavorToken(){
        this.favorToken = window.getSchema().getDifficulty();
    }

}
