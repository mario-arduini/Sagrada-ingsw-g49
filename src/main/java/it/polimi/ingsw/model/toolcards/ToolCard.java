package it.polimi.ingsw.model.toolcards;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.server.controller.exceptions.DisconnectionException;
import it.polimi.ingsw.server.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.server.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.RmiInterfaces.ClientInterface;
import it.polimi.ingsw.server.Logger;

import java.io.Serializable;
import java.util.List;

public class ToolCard implements Serializable {
    private String cardName;
    private transient JsonArray effects;
    private List<String> prerequisites;
    private transient Gson gson;
    private boolean used;
    private boolean rollback;
    private transient TransactionSnapshot gameTransaction;
    private transient Game realGame;
    private int i;

    public ToolCard(JsonObject toolCard){
        this.gson = new Gson();
        this.cardName = toolCard.get("name").getAsString();
        this.effects = toolCard.get("effects").getAsJsonArray();
        this.prerequisites = gson.fromJson(toolCard.get("prerequisites"),new TypeToken<List<String>>(){}.getType());
        this.rollback = toolCard.get("rollback").getAsBoolean();
        this.used = false;
    }

    public ToolCard(ToolCard toolCard){
        this.gson = new Gson();
        this.cardName = toolCard.cardName;
        this.effects = toolCard.effects;
        this.prerequisites = toolCard.prerequisites;
        this.rollback = toolCard.rollback;
        this.used = toolCard.used;
    }

    public String getName(){
        return this.cardName;
    }

    public void use(Game realGame, ClientInterface connection) throws NotEnoughFavorTokenException, InvalidFavorTokenNumberException, NoDiceInWindowException, NoDiceInRoundTrackException, NotYourSecondTurnException, AlreadyDraftedException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException, NotEnoughDiceToMoveException, PlayerSuspendedException, RollbackException, DisconnectionException {
        JsonObject effect;
        String command;
        JsonObject arguments = null;
        Dice multipurposeDice = null;
        this.realGame = realGame;
        this.gameTransaction = realGame.beginTransaction();
        this.gameTransaction.shuffleTheBag();
        Logger.print("Player " + gameTransaction.getRound().getCurrentPlayer().getNickname() + " using toolcard " + this.cardName);

        for (String prerequisite : prerequisites) {
            switch (prerequisite) {
                case "favor-token": Prerequisites.checkFavorToken(gameTransaction.getRound().getCurrentPlayer(), used ? 2 : 1); break;
                case "dice-window": Prerequisites.checkDiceInWindow(gameTransaction.getRound().getCurrentPlayer()); break;
                case "dice-round-track": Prerequisites.checkDiceInRoundTrack(gameTransaction.getRoundTrack()); break;
                case "same-color-window-track": Prerequisites.checkSameColorInWindowAndRoundTrack(gameTransaction.getRound().getCurrentPlayer(),gameTransaction.getRoundTrack()); break;
                case "first-turn": Prerequisites.checkFirstTurn(gameTransaction.getRound()); break;
                case "second-turn": Prerequisites.checkSecondTurn(gameTransaction.getRound()); break;
                case "before-draft": Prerequisites.checkBeforeDraft(gameTransaction.getRound().isDiceExtracted()); break;
                case "after-draft": Prerequisites.checkAfterDraft(gameTransaction.getRound().isDiceExtracted()); break;
                case "movable-color" : Prerequisites.checkMovable(gameTransaction.getRound().getCurrentPlayer(), Window.RuleIgnored.COLOR); break;
                case "movable-value" : Prerequisites.checkMovable(gameTransaction.getRound().getCurrentPlayer(), Window.RuleIgnored.NUMBER); break;
                case "movable" : Prerequisites.checkMovable(gameTransaction.getRound().getCurrentPlayer(), Window.RuleIgnored.NONE); break;
                case "two-dices-window" : Prerequisites.checkTwoDiceInWindow(gameTransaction.getWindow()); break;
            }

        }


        for (i = 0; i < effects.size(); i++) {
            effect = effects.get(i).getAsJsonObject();
            command = effect.keySet().toArray()[0].toString();
            TransactionSnapshot game = new TransactionSnapshot(gameTransaction);
            try {
                arguments = effect.get(command).getAsJsonObject();
            }catch (NullPointerException e) {
                Logger.print("ToolCard " + e);
            }
            Boolean optional = arguments.get("optional")!=null ? arguments.get("optional").getAsBoolean() : false;
            try {

                switch (command) {
                    case "get-draft-dice":
                        Effects.getDraftedDice(game.getRound(), connection, rollback);
                        break;
                    case "place-dice":
                        Window.RuleIgnored ruleIgnored;
                        if (gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class) != null)
                            ruleIgnored = gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class);
                        else
                            ruleIgnored = Window.RuleIgnored.NONE;
                        if (!Effects.addDiceToWindow(game.getWindow(), game.getRound().getCurrentDiceDrafted(), connection, ruleIgnored, rollback)) {
                            putDiceInDraftPool(game, connection);
                        }
                        break;
                    case "move":
                        if (arguments.get("color-in-track") != null && arguments.get("color-in-track").getAsBoolean()) {
                            multipurposeDice = Effects.move(game.getWindow(), game.getRoundTrack(), multipurposeDice, gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        }
                        Effects.move(game.getWindow(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        break;
                    case "move-n":
                        if (arguments.get("color-in-track") != null && arguments.get("color-in-track").getAsBoolean())
                            Effects.moveNColor(arguments.get("number").getAsInt(), game.getWindow(), game.getRoundTrack(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        else
                            Effects.moveN(arguments.get("number").getAsInt(), game.getWindow(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        break;
                    case "change-value":
                        if (arguments.get("plus") != null && arguments.get("plus").getAsBoolean())
                            Effects.changeValue(game.getRound().getCurrentDiceDrafted(), arguments.get("value").getAsInt(), connection, rollback);
                        else if (arguments.get("random") != null && arguments.get("random").getAsBoolean())
                            Effects.changeValue(game.getRound().getCurrentDiceDrafted(), connection);
                        break;
                    case "flip":
                        Effects.flip(game.getRound().getCurrentDiceDrafted(), connection);
                        break;
                    case "remove-turn":
                        game.getRound().removeTurn();
                        break;
                    case "reroll-pool":
                        Effects.rerollPool(game.getRound().getDraftPool());
                        break;
                    case "swap-round-dice":
                        Effects.swapRoundTrack(game.getRound(), game.getRoundTrack(), connection, rollback);
                        break;
                    case "put-in-bag":
                        game.putInBag(game.getRound().getCurrentDiceDrafted());
                        break;
                    case "set-from-bag":
                        Effects.setDiceFromBag(game.getRound(), game.getFromBag(), connection, rollback);
                        break;
                }
            }catch (RollbackException e){
                if (this.rollback)
                    throw e;
                i -= 1;
            }
            if (!game.getRound().getCurrentPlayer().equals(realGame.getCurrentRound().getCurrentPlayer())){
                throw new PlayerSuspendedException();
            }
            gameTransaction.commit(game);
        }
        try {
            realGame.commit(gameTransaction, cardName);
            Logger.print("Player " + gameTransaction.getRound().getCurrentPlayer().getNickname() + " successfully used " + this.cardName);
        } catch (NoSuchToolCardException e) {
            Logger.print("Toolcard " + cardName + " played by " + gameTransaction.getRound().getCurrentPlayer().getNickname() + "throws " + e.toString());
        }

    }

    public void continueToolCard(ClientInterface connection) throws NotEnoughFavorTokenException, InvalidFavorTokenNumberException, NoDiceInWindowException, NoDiceInRoundTrackException, NotYourSecondTurnException, AlreadyDraftedException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException, NotEnoughDiceToMoveException, PlayerSuspendedException, RollbackException, DisconnectionException {
        JsonObject effect;
        String command;
        JsonObject arguments = null;
        Dice multipurposeDice = null;

        for (; i < effects.size(); i++) {
            TransactionSnapshot game = new TransactionSnapshot(gameTransaction);
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
                        Effects.getDraftedDice(game.getRound(), connection, rollback);
                        break;
                    case "place-dice":
                        Window.RuleIgnored ruleIgnored;
                        if (gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class) != null)
                            ruleIgnored = gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class);
                        else
                            ruleIgnored = Window.RuleIgnored.NONE;
                        if (!Effects.addDiceToWindow(game.getWindow(), game.getRound().getCurrentDiceDrafted(), connection, ruleIgnored, rollback)) {
                            putDiceInDraftPool(game, connection);
                        }
                        break;
                    case "move":
                        if (arguments.get("color-in-track") != null && arguments.get("color-in-track").getAsBoolean()) {
                            multipurposeDice = Effects.move(game.getWindow(), game.getRoundTrack(), multipurposeDice, gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        }
                        Effects.move(game.getWindow(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        break;
                    case "move-n":
                        if (arguments.get("color-in-track") != null && arguments.get("color-in-track").getAsBoolean())
                            Effects.moveNColor(arguments.get("number").getAsInt(), game.getWindow(), game.getRoundTrack(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        else
                            Effects.moveN(arguments.get("number").getAsInt(), game.getWindow(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        break;
                    case "change-value":
                        if (arguments.get("plus") != null && arguments.get("plus").getAsBoolean())
                            Effects.changeValue(game.getRound().getCurrentDiceDrafted(), arguments.get("value").getAsInt(), connection, rollback);
                        else if (arguments.get("random") != null && arguments.get("random").getAsBoolean())
                            Effects.changeValue(game.getRound().getCurrentDiceDrafted(), connection);
                        break;
                    case "flip":
                        Effects.flip(game.getRound().getCurrentDiceDrafted(), connection);
                        break;
                    case "remove-turn":
                        game.getRound().removeTurn();
                        break;
                    case "reroll-pool":
                        Effects.rerollPool(game.getRound().getDraftPool());
                        break;
                    case "swap-round-dice":
                        Effects.swapRoundTrack(game.getRound(), game.getRoundTrack(), connection, rollback);
                        break;
                    case "put-in-bag":
                        game.putInBag(game.getRound().getCurrentDiceDrafted());
                        break;
                    case "set-from-bag":
                        Effects.setDiceFromBag(game.getRound(), game.getFromBag(), connection, rollback);
                        break;
                }
            }catch (RollbackException e){
                if (this.rollback)
                    throw e;
                i -= 1;
            }
            if (!game.getRound().getCurrentPlayer().equals(realGame.getCurrentRound().getCurrentPlayer())){
                throw new PlayerSuspendedException();
            }
            gameTransaction.commit(game);
        }
        try {
            realGame.commit(gameTransaction, cardName);
        } catch (NoSuchToolCardException e) {
            Logger.print("Toolcard " + cardName + " played by " + gameTransaction.getRound().getCurrentPlayer().getNickname() + "throws " + e.toString());
        }
    }

    public boolean getUsed(){
        return used;
    }

    public void setUsed(){
        this.used = true;
    }

    private void putDiceInDraftPool(TransactionSnapshot game, ClientInterface connection) throws DisconnectionException{
        game.getRound().getDraftPool().add(game.getRound().getCurrentDiceDrafted());
        game.getRound().setCurrentDiceDrafted(null);
        try {
            connection.alertDiceInDraftPool(game.getRound().getCurrentDiceDrafted());
        } catch (Exception e) {
            throw new DisconnectionException();
        }
    }
}
