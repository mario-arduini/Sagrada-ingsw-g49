package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransactionSnapshot {
    private Window window;
    private Round round;
    private List<Dice> roundTrack;
    private List<Dice> diceBag;

    public TransactionSnapshot(Game game, List<Dice> diceBag){
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

    public Window getWindow() {
        return window;
    }

    public Round getRound() {
        return round;
    }

    public List<Dice> getRoundTrack() {
        return roundTrack;
    }

    public void putInBag(Dice dice){
        dice.roll();
        diceBag.add(new Random().nextInt(diceBag.size()),dice);
    }

    public Dice getFromBag(){
        return diceBag.remove(diceBag.size()-1);
    }

    public List<Dice> getDiceBag() {
        return diceBag;
    }
}
