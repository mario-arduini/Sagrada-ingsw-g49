package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Round;

import java.util.List;

public class GlazingHammer extends ToolCard {
    public String getName(){
        return this.getClass().getName();
    }

    @Override
    public boolean canBeUsed(Round round){

        // Check if it is its second turn and it has not draft any dice yet
        if(round.getCurrentPlayerIndex()<round.getPlayersNumber()||round.getCurrentDiceDrafted()!=null)
            return false;
        // Check if enough favor tokens and remove them
        return super.canBeUsed(round);

    }

    @Override
    public boolean use(Round round){
        // check if tool card can be used and in case remove favor tokens
        if(!canBeUsed(round)) return false;

        List<Dice> draftPool = round.getDraftPool();

        for(Dice dice : draftPool){
            dice.roll();
        }

        return true;

    }
}
