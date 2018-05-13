package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Player;

public abstract class ToolCard {
    protected boolean used;

    public ToolCard(){
        this.used = false;
    }

    public String getName(){
        return "";
    }

    public boolean canBeUsed(Player player){
        return false;
    }

    public void use(Player player){

    }
}
