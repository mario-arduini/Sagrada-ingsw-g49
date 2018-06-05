package it.polimi.ingsw.model.toolcards;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.*;

import java.util.ArrayList;
import java.util.List;

public class ToolCard {
    private String cardName;
    private boolean used;
    private boolean useAfterDraft;
    private JsonArray effects;
    private List<String> prerequisites;
    private Gson gson;

    public ToolCard(JsonObject toolCard){
        this.gson = new Gson();
        this.cardName = toolCard.get("name").getAsString();
        this.useAfterDraft = toolCard.get("use-after-draft").getAsBoolean();
        this.effects = toolCard.get("effects").getAsJsonArray();
        this.prerequisites = gson.fromJson(toolCard.get("prerequisites"),new TypeToken<List<String>>(){}.getType());
        this.used = false;
    }

    public String getName(){
        return this.cardName;
    }

    public void use(Game game) throws InvalidDiceValueException, NotEnoughFavorTokenException, InvalidFavorTokenNumberException, NoDiceInWindowException, NoDiceInRoundTrackException, NotYourSecondTurnException, AlreadyDraftedException, BadAdjacentDiceException, ConstraintViolatedException, NoAdjacentDiceException, NotWantedAdjacentDiceException, FirstDiceMisplacedException {
        JsonObject effect;
        String command;
        JsonObject arguments;
        Dice dice = null;
        for (String prerequisite : prerequisites) {
            switch (prerequisite) {
                case "favor-token": Prerequisites.checkFavorToken(game.getCurrentRound().getCurrentPlayer(), used ? 2 : 1); break;
                case "dice-window": Prerequisites.checkDiceInWindow(game.getCurrentRound().getCurrentPlayer()); break;
                case "dice-round-track": Prerequisites.checkDiceInRoundTrack(game); break;
                case "second-turn": Prerequisites.checkSecondTurn(game.getCurrentRound()); break;
                case "before-draft": Prerequisites.checkBeforeDraft(game.getCurrentRound()); break;
            }
        }

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
                    dice = game.getCurrentRound().getCurrentDiceDrafted();
                    break;
                case "remove-turn":
                    game.getCurrentRound().removeTurn();
                    break;
                case "get-draft-dice":
                    Effects.getDraftedDice(game.getCurrentRound());
                    break;
                case "place-dice":
                    Effects.addDiceToWindow(game.getCurrentRound());
                    break;
                case "move":
                    break;
            }
        }
    }

}
