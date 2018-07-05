package it.polimi.ingsw.model.toolcards;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.controller.GameFlowHandler;
import it.polimi.ingsw.controller.GameRoom;
import it.polimi.ingsw.controller.exceptions.DisconnectionException;
import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.RMIInterfaces.ClientInterface;
import it.polimi.ingsw.network.server.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ToolCard implements Serializable {
    private String cardName;
    private JsonArray effects;
    private List<String> prerequisites;
    private Gson gson;
    private ClientInterface connection;
    private boolean used;
    private boolean awake;
    private Timer timer;
    private int secondsTimer;
    private boolean rollback;

    public ToolCard(JsonObject toolCard){
        this.gson = new Gson();
        this.cardName = toolCard.get("name").getAsString();
        this.effects = toolCard.get("effects").getAsJsonArray();
        this.prerequisites = gson.fromJson(toolCard.get("prerequisites"),new TypeToken<List<String>>(){}.getType());
        this.rollback = toolCard.get("rollback").getAsBoolean();
        this.connection = null;
        this.used = false;
        this.awake = true;
        this.timer = null;
        this.secondsTimer = 10; //TODO: CONFIG FILE
        this.awake = true;
    }

    public ToolCard(ToolCard toolCard){
        this.gson = new Gson();
        this.cardName = toolCard.cardName;
        this.effects = toolCard.effects;
        this.prerequisites = toolCard.prerequisites;
        this.rollback = toolCard.rollback;
        this.connection = null;
        this.used = toolCard.used;
        this.awake = true;
        this.timer = null;
        this.secondsTimer = 10; //TODO: CONFIG FILE
    }

    public String getName(){
        return this.cardName;
    }

    public synchronized void use(Game realGame, GameFlowHandler gameFlow) throws NotEnoughFavorTokenException, InvalidFavorTokenNumberException, NoDiceInWindowException, NoDiceInRoundTrackException, NotYourSecondTurnException, AlreadyDraftedException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException, NotEnoughDiceToMoveException, PlayerSuspendedException, RollbackException {
        JsonObject effect;
        String command;
        JsonObject arguments = null;
        Dice multipurposeDice = null;
        this.connection = gameFlow.getConnection();

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
                        Effects.getDraftedDice(game.getRound(), connection, rollback);
                        break;
                    case "place-dice":
                        Window.RuleIgnored ruleIgnored;
                        if (gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class) != null)
                            ruleIgnored = gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class);
                        else
                            ruleIgnored = Window.RuleIgnored.NONE;
                        if (!Effects.addDiceToWindow(game.getWindow(), game.getRound().getCurrentDiceDrafted(), connection, ruleIgnored, rollback)) {
                            game.getRound().getDraftPool().add(game.getRound().getCurrentDiceDrafted());
                            game.getRound().setCurrentDiceDrafted(null);
                        }
                        break;
                    case "move":
                        if (arguments.get("color-in-track") != null && arguments.get("color-in-track").getAsBoolean()) {
                            multipurposeDice = Effects.move(game.getWindow(), game.getRoundTrack(), multipurposeDice, gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        }
                        Effects.move(game.getWindow(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        break;
                    case "move-n":
                        Effects.moveN(arguments.get("number").getAsInt(), game.getWindow(), gson.fromJson(arguments.get("ignore"), Window.RuleIgnored.class), optional, connection, rollback);
                        break;
                    case "change-value":
                        if (arguments.get("plus") != null && arguments.get("plus").getAsBoolean())
                            Effects.changeValue(game.getRound().getCurrentDiceDrafted(), arguments.get("value").getAsInt(), connection, rollback);
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
            catch (DisconnectionException e){
                try {
                    this.awake = false;
                    startTimer();
                    wait();
                } catch (InterruptedException e1) {
                    Logger.print("Interrupted Exception Toolcard, awake.");
                }
                this.connection = gameFlow.getConnection();
                if (gameFlow.getPlayer().isSuspended()) throw new PlayerSuspendedException();
                i -= 1;
                this.awake = true;
            }
            if (!game.getRound().getCurrentPlayer().equals(realGame.getCurrentRound().getCurrentPlayer())){
                throw new PlayerSuspendedException();
            }
        }
        try {
            realGame.commit(game, cardName);
        } catch (NoSuchToolCardException e) {
            Logger.print("Toolcard " + game + "throws " + e.toString());
        }
    }


    public boolean getUsed(){
        return this.used;
    }

    public void setUsed(){
        this.used = true;
    }

    private void startTimer(){
        timer = new Timer();
        timer.schedule(new ToolCard.TimerExpired(), (long) secondsTimer * 1000);
    }

    class TimerExpired extends TimerTask {
        public void run() {
            if (!awake)
                this.notify();
        }
    }
}
