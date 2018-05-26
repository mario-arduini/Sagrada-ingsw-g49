package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.InvalidFavorTokenNumberException;
import it.polimi.ingsw.model.exceptions.NotEnoughFavorTokenException;

public abstract class ToolCard {
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
        return canBeUsed(round);
    }

    public boolean isUsedAfterDraft(){
        return false;
    }
}
