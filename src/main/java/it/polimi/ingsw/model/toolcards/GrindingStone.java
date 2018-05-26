package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;

public class GrindingStone extends ToolCard {

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

        try {
            currentDice.setValue(7-currentDice.getValue());
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
