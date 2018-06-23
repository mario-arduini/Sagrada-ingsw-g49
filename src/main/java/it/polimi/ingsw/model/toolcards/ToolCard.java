package it.polimi.ingsw.model.toolcards;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.server.ConnectionHandler;
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

    public void use(Game game, ConnectionHandler connection) throws NotEnoughFavorTokenException, InvalidFavorTokenNumberException, NoDiceInWindowException, NoDiceInRoundTrackException, NotYourSecondTurnException, AlreadyDraftedException, BadAdjacentDiceException, ConstraintViolatedException, NoAdjacentDiceException, NotWantedAdjacentDiceException, FirstDiceMisplacedException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException {
        JsonObject effect;
        String command;
        JsonObject arguments = null;
        Dice multipurposeDice = null;
        for (String prerequisite : prerequisites) {
            switch (prerequisite) {
                case "favor-token": Prerequisites.checkFavorToken(game.getCurrentRound().getCurrentPlayer(), used ? 2 : 1); break;
                case "dice-window": Prerequisites.checkDiceInWindow(game.getCurrentRound().getCurrentPlayer()); break;
                case "dice-round-track": Prerequisites.checkDiceInRoundTrack(game.getRoundTrack()); break;
                case "same-color-window-track": Prerequisites.checkSameColorInWindowAndRoundTrack(game.getCurrentRound().getCurrentPlayer(),game.getRoundTrack()); break;
                case "first-turn": Prerequisites.checkFirstTurn(game.getCurrentRound()); break;
                case "second-turn": Prerequisites.checkSecondTurn(game.getCurrentRound()); break;
                case "before-draft": Prerequisites.checkBeforeDraft(game.getCurrentRound().isDiceExtracted()); break;
                case "after-draft": Prerequisites.checkAfterDraft(game.getCurrentRound().isDiceExtracted()); break;
                case "movable-color" : Prerequisites.checkMovable(game.getCurrentRound().getCurrentPlayer(), Window.RuleIgnored.COLOR); break;
                case "movable-value" : Prerequisites.checkMovable(game.getCurrentRound().getCurrentPlayer(), Window.RuleIgnored.NUMBER); break;
                case "movable" : Prerequisites.checkMovable(game.getCurrentRound().getCurrentPlayer(), Window.RuleIgnored.NONE); break;
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
            Boolean optional = arguments.get("optional")!=null ? arguments.get("optional").getAsBoolean() : false;
            switch (command) {
                case "get-draft-dice":
                    Effects.getDraftedDice(game.getCurrentRound(), connection);
                    break;
                case "place-dice":
                    if(!Effects.addDiceToWindow(game.getCurrentRound().getCurrentPlayer(),game.getCurrentRound().getCurrentDiceDrafted(), connection)){
                        game.getCurrentRound().getDraftPool().add(game.getCurrentRound().getCurrentDiceDrafted());
                        game.getCurrentRound().setCurrentDiceDrafted(null);
                    }
                    break;
                case "move":
                    if(arguments.get("color-in-track")!=null&&arguments.get("color-in-track").getAsBoolean()){
                        multipurposeDice = Effects.move(game.getCurrentRound().getCurrentPlayer(),game.getRoundTrack(),multipurposeDice, gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class),optional, connection);
                    }
                    Effects.move(game.getCurrentRound().getCurrentPlayer(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class),optional, connection);
                    break;
                case "change-value":
                    if (arguments.get("plus").getAsBoolean())
                        Effects.changeValue(game.getCurrentRound().getCurrentDiceDrafted(), arguments.get("value").getAsInt(), connection);
                    else if(arguments.get("random")!=null&&arguments.get("random").getAsBoolean())
                        Effects.changeValue(game.getCurrentRound().getCurrentDiceDrafted());
                    break;
                case "flip":
                    Effects.flip(game.getCurrentRound().getCurrentDiceDrafted());
                    break;
                case "remove-turn":
                    game.getCurrentRound().removeTurn();
                    break;
                case "reroll-pool":
                    Effects.rerollPool(game.getCurrentRound().getDraftPool());
                    break;
                case "swap-round-dice":
                    Effects.swapRoundTrack(game.getCurrentRound(),game.getRoundTrack(), connection);
                    break;
                case "put-in-bag":
                    game.putInBag(game.getCurrentRound().getCurrentDiceDrafted());
                    break;
                case "get-from-bag":
                    Effects.getDiceFromBag(game.getCurrentRound(),game.getFromBag(), connection);
                    break;
            }
        }
        game.getCurrentRound().getCurrentPlayer().useFavorToken(used ? 2 : 1);
        this.used = true;
    }

}
