package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Round;

public class GrozingPliers extends ToolCard {
    public String getName(){
        return this.getClass().getName();
    }

    public boolean canBeUsed(Round round){
        return false;
    }

    public boolean use(Round round){

        return false;
    }
}
