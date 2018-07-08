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

/**
 * Class representing a ToolCard in the game
 */
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

    /**
     * Create a toolcard from a json object
     * @param toolCard jsonObject to use
     */
    public ToolCard(JsonObject toolCard){
        this.gson = new Gson();
        this.cardName = toolCard.get("name").getAsString();
        this.effects = toolCard.get("effects").getAsJsonArray();
        this.prerequisites = gson.fromJson(toolCard.get("prerequisites"),new TypeToken<List<String>>(){}.getType());
        this.rollback = toolCard.get("rollback").getAsBoolean();
        this.used = false;
    }

    /**
     * Duplicate a toolcard
     * @param toolCard card to duplicate
     */
    public ToolCard(ToolCard toolCard){
        this.gson = new Gson();
        this.cardName = toolCard.cardName;
        this.effects = toolCard.effects;
        this.prerequisites = toolCard.prerequisites;
        this.rollback = toolCard.rollback;
        this.used = toolCard.used;
    }

    /**
     * Get toolcard name
     * @return name of the ToolCard
     */
    public String getName(){
        return this.cardName;
    }

    /**
     * Try to use the ToolCard
     * @param realGame Game on which use the ToolCard
     * @param connection Connection of the Player using the ToolCard
     * @throws NotEnoughFavorTokenException signals active Player has not enough favor token
     * @throws InvalidFavorTokenNumberException signals an invalid number of favor token to use
     * @throws NoDiceInWindowException signals active Player has no Dice in his window, altough it is requested by the ToolCard
     * @throws NoDiceInRoundTrackException signals there are no dice in the round track, altough it is requested by the ToolCard
     * @throws NotYourSecondTurnException signals active Player is not in his second turn, altough it is requested by the ToolCard
     * @throws AlreadyDraftedException signals active Player has already extracted a Dice, altough it is requested by the ToolCard that he has not
     * @throws NotDraftedYetException signals active Player has not extracted a Dice yet, altough it is requested by the ToolCard
     * @throws NotYourFirstTurnException signals is not first turn of the active Player, altough it is requested by the ToolCard
     * @throws NoSameColorDicesException signals active Player has no Dice of the requested Color in his window, altough it is requested by the ToolCard
     * @throws NothingCanBeMovedException signals active Player has no Dices that can be moved in his window, altough it is requested by the ToolCard
     * @throws NotEnoughDiceToMoveException signals active Player has not enough Dices in his window, altough it is requested by the ToolCard
     * @throws PlayerSuspendedException signals active Player has been suspended
     * @throws RollbackException signals active Player asked for a rollback
     * @throws DisconnectionException signals active Player has disconnected
     */
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

    /**
     * Continue the use of a ToolCard after a disconnection in the middle of the ToolCard and a subsequent reconnection
     * @param connection Connection of the Player using the ToolCard
     * @throws NotEnoughFavorTokenException signals active Player has not enough favor token
     * @throws InvalidFavorTokenNumberException signals an invalid number of favor token to use
     * @throws NoDiceInWindowException signals active Player has no Dice in his window, altough it is requested by the ToolCard
     * @throws NoDiceInRoundTrackException signals there are no dice in the round track, altough it is requested by the ToolCard
     * @throws NotYourSecondTurnException signals active Player is not in his second turn, altough it is requested by the ToolCard
     * @throws AlreadyDraftedException signals active Player has already extracted a Dice, altough it is requested by the ToolCard that he has not
     * @throws NotDraftedYetException signals active Player has not extracted a Dice yet, altough it is requested by the ToolCard
     * @throws NotYourFirstTurnException signals is not first turn of the active Player, altough it is requested by the ToolCard
     * @throws NoSameColorDicesException signals active Player has no Dice of the requested Color in his window, altough it is requested by the ToolCard
     * @throws NothingCanBeMovedException signals active Player has no Dices that can be moved in his window, altough it is requested by the ToolCard
     * @throws NotEnoughDiceToMoveException signals active Player has not enough Dices in his window, altough it is requested by the ToolCard
     * @throws PlayerSuspendedException signals active Player has been suspended
     * @throws RollbackException signals active Player asked for a rollback
     * @throws DisconnectionException signals active Player has disconnected
     */
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

    /**
     * Check if toolcard has been used already
     * @return true if has been used, false otherwise
     */
    public boolean getUsed(){
        return used;
    }

    /**
     * set ToolCard as used
     */
    public void setUsed(){
        this.used = true;
    }

    /**
     * Put the extracted Dice in the draftpool and notify the Connection
     * @param game Current Game
     * @param connection Active Player Connection
     * @throws DisconnectionException signals active player has disconnected
     */
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
