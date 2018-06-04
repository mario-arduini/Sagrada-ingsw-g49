package it.polimi.ingsw.network.client;

import com.google.gson.JsonObject;

public class ToolCard {
    private String cardName;
    private String description;
    private boolean used;

    public ToolCard(String name, String description){
        this.cardName = name;
        this.description = description;
        this.used = false;
    }

    public String getName(){
        return this.cardName;
    }

    public String getDescription() { return this.description; }

    public void setUsed() { used = true; }
}
