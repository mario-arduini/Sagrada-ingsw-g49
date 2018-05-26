package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.toolcards.ToolCard;

import java.util.ArrayList;
import java.util.List;

public class Round {
    private List<Dice> draftPool;
    private List<Player> players;
    private int currentPlayer;
    private boolean diceExtracted;
    private Dice currentDiceDrafted;
    private ToolCard activeToolCard;
    private int playersNumber;

    public Round(List<Dice> draftPool, List<Player> players){
        this.draftPool = new ArrayList<>(draftPool);
        this.players = new ArrayList<>(players);
        for(int i = players.size() - 1; i >= 0; i--)
            this.players.add(this.players.get(i));
        diceExtracted = false;
        currentDiceDrafted = null;
        currentPlayer = -1;
        activeToolCard = null;
        playersNumber = players.size()/2;
    }

    public Round(Round round){
        this.draftPool = new ArrayList<>(round.draftPool);
        this.players = new ArrayList<>(round.players);
        this.diceExtracted = round.diceExtracted;
        this.currentPlayer = round.currentPlayer;
        this.currentDiceDrafted = round.currentDiceDrafted;
        this.activeToolCard = round.activeToolCard;
        this.playersNumber = round.playersNumber;
    }

    public List<Dice> getDraftPool(){
        return draftPool;
    }

    public int getCurrentPlayerIndex() { return currentPlayer; }

    public Player getCurrentPlayer() { return players.get(currentPlayer); }

    public int getPlayersNumber() { return playersNumber; }

    public Dice getCurrentDiceDrafted() { return currentDiceDrafted; }

    public void setActiveToolCard(ToolCard toolCard){
        activeToolCard = toolCard;
    }

    public Player nextPlayer() throws NoMorePlayersException {
        currentPlayer++;
        if(currentPlayer == players.size()) {
            throw new NoMorePlayersException();
        }
        diceExtracted = false;
        activeToolCard = null;
        currentDiceDrafted = null;
        if(players.get(currentPlayer).isSuspended())
            return nextPlayer();
        return players.get(currentPlayer);
    }

    public boolean isLastTurn(){
        return currentPlayer == players.size() - 1;
    }


    /* TODO CONSIDER THIS FUNCTION
    public void chooseFromDraft(Dice dice) throws DiceNotInDraftPoolException, DiceAlreadyExtractedException {
        // TODO: consider choices change
        if(diceExtracted)
            throw new DiceAlreadyExtractedException();
        if (!draftPool.contains(dice))
            throw new DiceNotInDraftPoolException();

        draftPool.remove(dice);
        currentDiceDrafted = dice;

        // use "after draft" type toolcards
        if(activeToolCard!=null && activeToolCard.isUsedAfterDraft()){
            activeToolCard.use(this);
        }

    } */

    public void useDice(int row, int column, Dice dice) throws NoAdjacentDiceException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, DiceNotInDraftPoolException, DiceAlreadyExtractedException {

        if(diceExtracted)
            throw new DiceAlreadyExtractedException();
        if (!draftPool.contains(dice))
            throw new DiceNotInDraftPoolException();

        draftPool.remove(dice);
        currentDiceDrafted = dice;

        // use "after draft" type toolcards
        if(activeToolCard!=null && activeToolCard.isUsedAfterDraft()){
            activeToolCard.use(this);
        }

        players.get(currentPlayer).getWindow().addDice(row,column,currentDiceDrafted);

        diceExtracted = true;
    }

    public void suspendPlayer(){
        players.get(currentPlayer).suspend();
    }

    @Override
    public boolean equals(Object round) {

        if(!(round instanceof Round))
            return false;

        return this.draftPool.equals(((Round) round).draftPool) && this.players.equals(((Round) round).players) && this.diceExtracted == ((Round) round).diceExtracted;
    }
}
