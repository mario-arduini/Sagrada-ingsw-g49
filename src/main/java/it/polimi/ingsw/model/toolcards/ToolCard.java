package it.polimi.ingsw.model.toolcards;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import it.polimi.ingsw.model.exceptions.InvalidFavorTokenNumberException;
import it.polimi.ingsw.model.exceptions.NotEnoughFavorTokenException;

import java.util.ArrayList;
import java.util.List;

public class ToolCard {
    private String cardName;
    private boolean used;
    private boolean useAfterDraft;
    private JsonArray effects;

    public ToolCard(JsonObject toolCard){
        this.cardName = toolCard.get("name").getAsString();
        this.useAfterDraft = toolCard.get("use-after-draft").getAsBoolean();
        this.effects = toolCard.get("effects").getAsJsonArray();
        this.used = false;
    }

    public String getName(){
        return this.cardName;
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

    public void use(Round round) throws InvalidDiceValueException {
        JsonObject effect;
        String command;
        JsonObject arguments;
        Dice dice = null;

        for (int i = 0; i < effects.size(); i++) {
            effect = effects.get(i).getAsJsonObject();
            command = effect.keySet().toString();
            arguments = effect.get(command).getAsJsonObject();
            switch (command) {
                case "change-value":
                    Effects.changeValue(dice, arguments.get("plus").getAsBoolean(), arguments.get("value").getAsInt());
                    break;
                case "flip":
                    Effects.flip(dice);
                    break;
                case "get-dice-from-round":
                    dice = round.getCurrentDiceDrafted();
                    break;
                case "remove-turn":
                    round.removeTurn();
                    break;

            }
        }
    }

    public boolean isUsedAfterDraft(){
        return this.useAfterDraft;
    }
}
