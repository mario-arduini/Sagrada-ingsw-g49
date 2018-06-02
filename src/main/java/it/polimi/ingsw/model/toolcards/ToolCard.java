package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.InvalidFavorTokenNumberException;
import it.polimi.ingsw.model.exceptions.NotEnoughFavorTokenException;

public class ToolCard {
    protected boolean used;

    public ToolCard(){
        this.used = false;
    }

    public String getName(){
        return this.getClass().getName();
    }

    public boolean canBeUsed(Round round){
        // try to use favorToken
        try {
            round.getCurrentPlayer().useFavorToken(used ? 2 : 1);
            if(!used) used=true;
            return true;
        } catch (NotEnoughFavorTokenException e) {
            return false;
        } catch (InvalidFavorTokenNumberException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean use(Round round){
        return false;
    }

    public boolean use(Round round,int x1_start,int y1_start,int x1_end,int y1_end,int x2_start,int y2_start,int x2_end,int y2_end){
        return false;
    }

    public boolean isUsedAfterDraft(){
        return false;
    }
}
