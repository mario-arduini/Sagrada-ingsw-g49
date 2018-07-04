package it.polimi.ingsw.model.toolcards;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.controller.exceptions.DisconnectionException;
import it.polimi.ingsw.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.RMIInterfaces.ClientInterface;
import it.polimi.ingsw.network.server.Logger;

import java.io.Serializable;
import java.util.List;

public class ToolCard implements Serializable {
    private String cardName;
    private boolean used;
    private JsonArray effects;
    private List<String> prerequisites;
    private Gson gson;
    private boolean suspended;

    public ToolCard(JsonObject toolCard){
        this.gson = new Gson();
        this.cardName = toolCard.get("name").getAsString();
        this.effects = toolCard.get("effects").getAsJsonArray();
        this.prerequisites = gson.fromJson(toolCard.get("prerequisites"),new TypeToken<List<String>>(){}.getType());
        this.used = false;
        this.suspended = false;
    }

    public String getName(){
        return this.cardName;
    }

    public void use(Game realGame, ClientInterface connection) throws NotEnoughFavorTokenException, InvalidFavorTokenNumberException, NoDiceInWindowException, NoDiceInRoundTrackException, NotYourSecondTurnException, AlreadyDraftedException, BadAdjacentDiceException, ConstraintViolatedException, NoAdjacentDiceException, NotWantedAdjacentDiceException, FirstDiceMisplacedException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException, NotEnoughDiceToMoveException {
        JsonObject effect;
        String command;
        JsonObject arguments = null;
        Dice multipurposeDice = null;


        for (String prerequisite : prerequisites) {
            switch (prerequisite) {
                case "favor-token": Prerequisites.checkFavorToken(realGame.getCurrentRound().getCurrentPlayer(), used ? 2 : 1); break;
                case "dice-window": Prerequisites.checkDiceInWindow(realGame.getCurrentRound().getCurrentPlayer()); break;
                case "dice-round-track": Prerequisites.checkDiceInRoundTrack(realGame.getRoundTrack()); break;
                case "same-color-window-track": Prerequisites.checkSameColorInWindowAndRoundTrack(realGame.getCurrentRound().getCurrentPlayer(),realGame.getRoundTrack()); break;
                case "first-turn": Prerequisites.checkFirstTurn(realGame.getCurrentRound()); break;
                case "second-turn": Prerequisites.checkSecondTurn(realGame.getCurrentRound()); break;
                case "before-draft": Prerequisites.checkBeforeDraft(realGame.getCurrentRound().isDiceExtracted()); break;
                case "after-draft": Prerequisites.checkAfterDraft(realGame.getCurrentRound().isDiceExtracted()); break;
                case "movable-color" : Prerequisites.checkMovable(realGame.getCurrentRound().getCurrentPlayer(), Window.RuleIgnored.COLOR); break;
                case "movable-value" : Prerequisites.checkMovable(realGame.getCurrentRound().getCurrentPlayer(), Window.RuleIgnored.NUMBER); break;
                case "movable" : Prerequisites.checkMovable(realGame.getCurrentRound().getCurrentPlayer(), Window.RuleIgnored.NONE); break;
                case "two-dices-window" : Prerequisites.checkTwoDiceInWindow(realGame.getCurrentRound().getCurrentPlayer().getWindow()); ; break;
            }
        }

        TransactionSnapshot game = realGame.beginTransaction();

        for (int i = 0; i < effects.size(); i++) {
            effect = effects.get(i).getAsJsonObject();
            command = effect.keySet().toArray()[0].toString();
            try {
                arguments = effect.get(command).getAsJsonObject();
            }catch (NullPointerException e) {
                Logger.print("ToolCard " + e);
            }
            Boolean optional = arguments.get("optional")!=null ? arguments.get("optional").getAsBoolean() : false;
            try {
                switch (command) {
                    case "get-draft-dice":
                        Effects.getDraftedDice(game.getRound(), connection);
                        break;
                    case "place-dice":
                        if (!Effects.addDiceToWindow(game.getWindow(), game.getRound().getCurrentDiceDrafted(), connection, gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class))) {
                            game.getRound().getDraftPool().add(game.getRound().getCurrentDiceDrafted());
                            game.getRound().setCurrentDiceDrafted(null);
                        }
                        break;
                    case "move":
                        if (arguments.get("color-in-track") != null && arguments.get("color-in-track").getAsBoolean()) {
                            multipurposeDice = Effects.move(game.getWindow(), game.getRoundTrack(), multipurposeDice, gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection);
                        }
                        Effects.move(game.getWindow(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection);
                        break;
                    case "change-value":
                        if (arguments.get("plus") != null && arguments.get("plus").getAsBoolean())
                            Effects.changeValue(game.getRound().getCurrentDiceDrafted(), arguments.get("value").getAsInt(), connection);
                        else if (arguments.get("random") != null && arguments.get("random").getAsBoolean())
                            Effects.changeValue(game.getRound().getCurrentDiceDrafted(), connection);
                        break;
                    case "flip":
                        Effects.flip(game.getRound().getCurrentDiceDrafted());
                        break;
                    case "remove-turn":
                        game.getRound().removeTurn();
                        break;
                    case "reroll-pool":
                        Effects.rerollPool(game.getRound().getDraftPool());
                        break;
                    case "swap-round-dice":
                        Effects.swapRoundTrack(game.getRound(), game.getRoundTrack(), connection);
                        break;
                    case "put-in-bag":
                        game.putInBag(game.getRound().getCurrentDiceDrafted());
                        break;
                    case "set-from-bag":
                        Effects.setDiceFromBag(game.getRound(), game.getFromBag(), connection);
                        break;

                }
            }catch (RollbackException e){
                return;
            }catch (DisconnectionException e){
                //TODO: do something pls :(
                i -= 1;
            }
            if (!game.getRound().getCurrentPlayer().equals(realGame.getCurrentRound().getCurrentPlayer())){
                return;
            }
        }
        synchronized (realGame) {
            if (!game.getRound().getCurrentPlayer().equals(realGame.getCurrentRound().getCurrentPlayer())){
                return;
            }
            realGame.commit(game);
            realGame.getCurrentRound().getCurrentPlayer().useFavorToken(used ? 2 : 1);
        }
        this.used = true;
    }



}
