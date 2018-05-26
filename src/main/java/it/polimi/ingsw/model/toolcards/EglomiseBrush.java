package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.Window;

public class EglomiseBrush extends ToolCard {

    @Override
    public boolean use(Round round,int x1_start,int y1_start,int x1_end,int y1_end,int x2_start,int y2_start,int x2_end,int y2_end){
        // check if tool card can be used and in case remove favor tokens
        if(!canBeUsed(round)) return false;

        // TODO consider canBePlaced + add instead of addDice
        Window playerWindow = round.getCurrentPlayer().getWindow();
        Dice toMove = playerWindow.getCell(x1_start,y1_start);

        if(toMove == null) return false;

        playerWindow.removeDice(x1_start,y1_start);

        try {
            playerWindow.addDiceIgnoreColorConstraint(x1_end,y1_end,toMove);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
