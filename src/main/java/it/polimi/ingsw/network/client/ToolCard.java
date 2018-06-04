package it.polimi.ingsw.network.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import it.polimi.ingsw.model.exceptions.InvalidFavorTokenNumberException;
import it.polimi.ingsw.model.exceptions.NotEnoughFavorTokenException;
import it.polimi.ingsw.model.toolcards.Effects;

public class ToolCard {
    private String cardName;
    private String description;
    private boolean used;

    public ToolCard(JsonObject toolCard){
        this.cardName = toolCard.get("name").getAsString();
        this.description = toolCard.get("description").getAsString();
        this.used = false;
    }

    public String getName(){
        return this.cardName;
    }

    public String getDescription() { return this.description; }
}
