package it.polimi.ingsw.model;

public class EglomiseBrush extends ToolCard{
    public String getName(){
        return this.getClass().getName();
    }

    public boolean canBeUsed(Player player){
        return false;
    }

    public void use(Player player){

    }
}
