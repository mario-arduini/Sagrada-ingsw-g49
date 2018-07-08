package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A Transaction Snapshot is an object that represents a game in a particular round status.
 * Can be used to modify a game status and commit changes later.
 */
public class TransactionSnapshot {
    private Window window;
    private Round round;
    private List<Dice> roundTrack;
    private List<Dice> diceBag;

    /**
     * Creates a Transaction Snapshot.
     * @param game game from which data are copied.
     * @param diceBag dice bag associated to the current game.
     */
    TransactionSnapshot(Game game, List<Dice> diceBag){
        this.window = new Window(game.getCurrentRound().getCurrentPlayer().getWindow());
        this.round = new Round(game.getCurrentRound());
        this.roundTrack = new ArrayList<>();
        for (Dice d: game.getRoundTrack())
            roundTrack.add(new Dice(d));
        this.diceBag = new ArrayList<>();
        for (Dice d: diceBag){
            this.diceBag.add(new Dice(d));
        }
    }

    /**
     * Creates a Transaction Snapshot of a Transaction Snapshot.
     * @param game Transaction Snapshot from which data are copied.
     */
    public TransactionSnapshot(TransactionSnapshot game){
        this.window = new Window(game.getRound().getCurrentPlayer().getWindow());
        this.round = new Round(game.getRound());
        this.roundTrack = new ArrayList<>();
        for (Dice d: game.getRoundTrack())
            roundTrack.add(new Dice(d));
        this.diceBag = new ArrayList<>();
        for (Dice d: game.getDiceBag()){
            this.diceBag.add(new Dice(d));
        }
    }

    /**
     * Commits changes from a different TransactionSnapshot.
     * @param game Transaction Snapshot with data to commit.
     */
    public void commit(TransactionSnapshot game){
        this.window = game.window;
        this.round = game.round;
        this.roundTrack = game.roundTrack;
        this.diceBag = game.diceBag;
    }

    /**
     * Returns the window in the current snapshot.
     * @return window
     */
    public Window getWindow() {
        return window;
    }

    /**
     * Returns the round associated to the game snapshot.
     * @return current round.
     */
    public Round getRound() {
        return round;
    }

    /**
     * Returns the round track associated to the game snapshot.
     * @return roundTrack as a list of Dice.
     */
    public List<Dice> getRoundTrack() {
        return roundTrack;
    }

    /**
     * Puts a dice into the bag.
     * @param dice the dice to be put in the bag.
     */
    public void putInBag(Dice dice){
        dice.roll();
        diceBag.add(new Random().nextInt(diceBag.size()),dice);
    }

    /**
     * Gets a dice from the dice bag. Dice is removed from the bag.
     * @return a dice from the dice bag.
     */
    public Dice getFromBag(){
        return diceBag.remove(diceBag.size()-1);
    }

    /**
     * Returns the dice bag.
     * @return diceBag as a list of dice.
     */
    List<Dice> getDiceBag() {
        return diceBag;
    }
}
