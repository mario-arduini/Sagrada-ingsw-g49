package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.Set;

public class Round {
    private Set<Dice> draftPool;
    private ArrayList<Player> players;
    private int currentPlayer;
    boolean lastTurn;
    boolean diceExtracted;

    public Round(Set<Dice> draftPool, ArrayList<Player> players){
        this.draftPool = draftPool;
        this.players = players;
    }

    public Set<Dice> getDraftPool(){
        return draftPool;
    }

    public boolean nextPlayer(){    //control this
        return lastTurn;
    }

    protected boolean extractDice(Dice choice){     //not implemented yet
        return true;
    }
}
