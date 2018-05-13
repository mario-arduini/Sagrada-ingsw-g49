package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Player;

public class FluxBrush extends ToolCard {
    public String getName(){
        return this.getClass().getName();
    }

    public boolean canBeUsed(Player player){
        return false;
    }

    public void use(Player player){

    }
}
