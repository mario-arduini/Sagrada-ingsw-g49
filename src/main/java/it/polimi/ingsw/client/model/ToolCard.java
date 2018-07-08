package it.polimi.ingsw.client.model;

public class ToolCard {
    private String cardName;
    private boolean used;

    public ToolCard(String name){
        this.cardName = name;
        this.used = false;
    }

    public String getName(){
        return this.cardName;
    }

    public void setUsed() { used = true; }

    public boolean getUsed() { return used; }
}
