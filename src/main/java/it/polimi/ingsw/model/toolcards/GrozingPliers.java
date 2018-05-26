package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Round;

public class GrozingPliers extends ToolCard {

    @Override
    public boolean isUsedAfterDraft(){
        return true;
    }

    @Override
    public boolean use(Round round){
        // check if tool card can be used and in case remove favor tokens
        if(!canBeUsed(round)) return false;

        Dice currentDice = round.getCurrentDiceDrafted();

        if(currentDice==null) return false;

        return true;
    }

}
