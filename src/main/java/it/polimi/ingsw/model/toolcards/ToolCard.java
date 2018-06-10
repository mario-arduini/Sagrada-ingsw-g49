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
import it.polimi.ingsw.network.server.Logger;

import java.util.ArrayList;
import java.util.List;

public class ToolCard {
    private String cardName;
    private boolean used;
    private JsonArray effects;
    private List<String> prerequisites;
    private Gson gson;

    public ToolCard(JsonObject toolCard){
        this.gson = new Gson();
        this.cardName = toolCard.get("name").getAsString();
        this.effects = toolCard.get("effects").getAsJsonArray();
        this.prerequisites = gson.fromJson(toolCard.get("prerequisites"),new TypeToken<List<String>>(){}.getType());
        this.used = false;
    }

    public String getName(){
        return this.cardName;
    }

    public void use(Game game) throws InvalidDiceValueException, NotEnoughFavorTokenException, InvalidFavorTokenNumberException, NoDiceInWindowException, NoDiceInRoundTrackException, NotYourSecondTurnException, AlreadyDraftedException, BadAdjacentDiceException, ConstraintViolatedException, NoAdjacentDiceException, NotWantedAdjacentDiceException, FirstDiceMisplacedException, NotDraftedYetException, NotYourFirstTurnException {
        JsonObject effect;
        String command;
        JsonObject arguments = null;
        for (String prerequisite : prerequisites) {
            switch (prerequisite) {
                case "favor-token": Prerequisites.checkFavorToken(game.getCurrentRound().getCurrentPlayer(), used ? 2 : 1); break;
                case "dice-window": Prerequisites.checkDiceInWindow(game.getCurrentRound().getCurrentPlayer()); break;
                case "dice-round-track": Prerequisites.checkDiceInRoundTrack(game.getRoundTrack()); break;
                case "first-turn": Prerequisites.checkFirstTurn(game.getCurrentRound()); break;
                case "second-turn": Prerequisites.checkSecondTurn(game.getCurrentRound()); break;
                case "before-draft": Prerequisites.checkBeforeDraft(game.getCurrentRound().isDiceExtracted()); break;
                case "after-draft": Prerequisites.checkAfterDraft(game.getCurrentRound().isDiceExtracted()); break;
            }
        }

        for (int i = 0; i < effects.size(); i++) {
            effect = effects.get(i).getAsJsonObject();
            command = effect.keySet().toArray()[0].toString();
            try {
                arguments = effect.get(command).getAsJsonObject();
            }catch (NullPointerException e) {
                Logger.print("ToolCard " + e);
            }
            switch (command) {
                case "change-value":
                    if (arguments.get("plus").getAsBoolean())
                        Effects.changeValue(game.getCurrentRound(), arguments.get("value").getAsInt());
                    else if(arguments.get("random").getAsBoolean())
                        Effects.changeValue(game.getCurrentRound());
                    break;
                case "flip":
                    Effects.flip(game.getCurrentRound());
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
                case "swap-round-dice":
                    Effects.swapRoundTrack(game);
                    break;
                case "move":
                    Effects.move(game.getCurrentRound(), gson.fromJson(arguments.get("ignore"), Effects.RuleIgnored.class));
                    break;
            }
        }
        this.used = true;
    }

}
