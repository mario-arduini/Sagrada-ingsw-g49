package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Round {
    private List<Dice> draftPool;
    private List<Player> players;
    private int currentPlayer;
    private boolean diceExtracted;

    public Round(List<Dice> draftPool, List<Player> players){
        this.draftPool = new ArrayList<>(draftPool);
        this.players = new ArrayList<>(players);
        for(int i = players.size() - 1; i >= 0; i--)
            this.players.add(this.players.get(i));
        diceExtracted = false;
        currentPlayer = -1;
    }

    public Round(Round round){
        this.draftPool = new ArrayList<>(round.draftPool);
        this.players = new ArrayList<>(round.players);
        this.diceExtracted = round.diceExtracted;
        this.currentPlayer = round.currentPlayer;
    }

    public List<Dice> getDraftPool(){

        return draftPool;
    }

    public Player nextPlayer() throws NoMorePlayersException {
        currentPlayer++;
        if(currentPlayer == players.size()) {
            throw new NoMorePlayersException();
        }
        diceExtracted = false;
        if(players.get(currentPlayer).isSuspended())
            return nextPlayer();
        return players.get(currentPlayer);
    }

    public boolean isLastTurn(){

        return currentPlayer == players.size() - 1;
    }

    public void useDice(int row, int column, Dice dice) throws NoAdjacentDiceException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, DiceNotInDraftPoolException, DiceAlreadyExtractedException {
        if(diceExtracted)
            throw new DiceAlreadyExtractedException();
        if (!draftPool.contains(dice))
            throw new DiceNotInDraftPoolException();

        players.get(currentPlayer).getWindow().addDice(row,column,dice);
        draftPool.remove(dice);
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
