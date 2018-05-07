package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.DiceAlreadyExtractedException;
import it.polimi.ingsw.model.exceptions.DiceNotInDraftPoolException;
import it.polimi.ingsw.model.exceptions.NoMorePlayersException;
import java.util.ArrayList;
import java.util.List;

public class Round {
    private List<Dice> draftPool;
    private ArrayList<Player> players;
    private boolean diceExtracted;

    public Round(List<Dice> draftPool, ArrayList<Player> players){
        this.draftPool = draftPool;
        this.players = players;
        diceExtracted = false;
    }

    public List<Dice> getDraftPool(){

        return draftPool;
    }

    public Player nextPlayer() throws NoMorePlayersException {
        if(players.size() <= 1) {
            throw new NoMorePlayersException();
        }
        Player player = players.get(1);
        players.remove(0);
        diceExtracted = false;
        return player;
    }

    public boolean isLastTurn(){

        return players.size() == 1;
    }

    public boolean isDiceExtracted(){
        return diceExtracted;
    }

    protected void useDice(Dice choice) throws DiceNotInDraftPoolException, DiceAlreadyExtractedException {
        if(diceExtracted)
            throw new DiceAlreadyExtractedException();
        for(Dice dice: draftPool)
            if(dice.equals(choice)) {
                draftPool.remove(choice);
                diceExtracted = true;
                return;
            }
        throw new DiceNotInDraftPoolException();
    }
}
