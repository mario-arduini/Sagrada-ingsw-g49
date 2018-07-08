package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a Round of the Game
 */
public class Round {
    private List<Dice> draftPool;
    private List<Player> players;
    private int currentPlayer;
    private boolean diceExtracted;
    private Dice currentDiceDrafted;
    private int playersNumber;

    /**
     * Round constructor given the Draft Pool and the list of players' turns
     * @param draftPool Draft Pool for the Round
     * @param players Ordered List of Players' turns
     */
    public Round(List<Dice> draftPool, List<Player> players){
        this.draftPool = draftPool;
        this.players = players;
        for(int i = players.size() - 1; i >= 0; i--)
            this.players.add(this.players.get(i));
        diceExtracted = false;
        currentDiceDrafted = null;
        currentPlayer = -1;
        playersNumber = players.size()/2;
    }

    /**
     * Duplicate the Round
     * @param round Round to duplicate
     */
    public Round(Round round){
        this.draftPool = new ArrayList<>(round.draftPool);
        this.players = new ArrayList<>(round.players);
        this.diceExtracted = round.diceExtracted;
        this.currentPlayer = round.currentPlayer;
        this.currentDiceDrafted = round.currentDiceDrafted;
        this.playersNumber = round.playersNumber;
    }


    /**
     * Get the Draft Pool
     * @return the Draft Pool as List of Dice
     */
    public List<Dice> getDraftPool(){
        return draftPool;
    }

    /**
     * Set a different Draft Pool
     * @param fake the New Draft Pool
     */
    public void fakeDraftPool(List <Dice> fake) { draftPool = fake; }

    /**
     * Get Current player index
     * @return current player index
     */
    public int getCurrentPlayerIndex() { return currentPlayer; }

    /**
     * Get current turn's palyer
     * @return current turn's Player
     */
    public Player getCurrentPlayer() { return players.get(currentPlayer); }

    /**
     * Get number of players
     * @return number of players
     */
    public int getPlayersNumber() { return playersNumber; }

    /**
     * Get the current drafted Dice
     * @return current drafted Dice or null
     */
    public Dice getCurrentDiceDrafted() { return currentDiceDrafted; }

    /**
     * Set the current drafted Dice
     * @param dice Dice to set as CurrentDraftedDice
     */
    public void setCurrentDiceDrafted(Dice dice) {
        currentDiceDrafted = dice;
    }

    /**
     * Check if Dice has already been extracted in the turn
     * @return true if Dice has already been extracted, false otherwise
     */
    public boolean isDiceExtracted(){
        return diceExtracted;
    }

    /**
     * Set the flag indicating that the Dice has been extracted in the turn
     * @param diceExtracted value of the flag
     */
    public void setDiceExtracted(boolean diceExtracted){
        this.diceExtracted = diceExtracted;
    }

    /**
     * Remove a turn from the round (for example as result of a toolcard's effect)
     */
    public void removeTurn() {
        for(int i = currentPlayer + 1; i < players.size(); i++)
            if(players.get(i).getNickname().equals(players.get(currentPlayer).getNickname()))
                players.remove(i);
    }

    /**
     * Pass to the next turn in the round
     * @return active Player of the Turn
     * @throws NoMorePlayersException signals that the round is over
     */
    public Player nextPlayer() throws NoMorePlayersException {
        currentPlayer++;
        if(currentPlayer == players.size()) {
            currentPlayer--;
            throw new NoMorePlayersException();
        }
        diceExtracted = false;
        currentDiceDrafted = null;
        if(players.get(currentPlayer).isSuspended())
            return nextPlayer();
        return players.get(currentPlayer);
    }

    /**
     * Check if it is the last turn in the round
     * @return true if it is the last turn, false otherwise
     */
    public boolean isLastTurn(){
        return currentPlayer == players.size() - 1;
    }

    /**
     * return the first position of the given Player in the Round
     * @param player Player which position is wanted
     * @return 0-based position of the Player in the Round
     */
    public int getPlayerPosition(Player player){
        return this.players.indexOf(player);
    }

    /**
     * Try to place the given Dice in the given cell of the current active Player window
     * @param row row of the cell
     * @param column column of the cell
     * @param dice Dice to be added
     * @throws NoAdjacentDiceException signals that a non-first dice is placed not adjacent to another dice
     * @throws BadAdjacentDiceException signals that one of the orthogonal was of the same color or value of dice
     * @throws ConstraintViolatedException signals that a constraint of the schema was not respected
     * @throws FirstDiceMisplacedException signals that the first was not place in the proper position (border of the window)
     * @throws DiceNotInDraftPoolException signals that the Dice is not present in the Draft Pool
     * @throws DiceAlreadyExtractedException signals that a Dice was already extracted in the current turn
     * @throws DiceAlreadyHereException signals the cell is already occupied
     */
    public void useDice(int row, int column, Dice dice) throws NoAdjacentDiceException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, DiceNotInDraftPoolException, DiceAlreadyExtractedException, DiceAlreadyHereException {
        if(diceExtracted)
            throw new DiceAlreadyExtractedException();
        if (!draftPool.contains(dice))
            throw new DiceNotInDraftPoolException();

        players.get(currentPlayer).getWindow().addDice(row, column, dice);

        currentDiceDrafted = dice;
        draftPool.remove(dice);
        diceExtracted = true;
    }

    /**
     * Suspend the current Player
     */
    public void suspendPlayer(){
        players.get(currentPlayer).suspend();
    }

    /**
     * Check if the given Round is equal to the caller
     * @param round Round to compare
     * @return true if the Rounds are equal, false otherwise
     */
    @Override
    public boolean equals(Object round) {

        return round instanceof Round && this.draftPool.equals(((Round) round).draftPool) && this.players.equals(((Round) round).players) && this.diceExtracted == ((Round) round).diceExtracted;

    }
}
