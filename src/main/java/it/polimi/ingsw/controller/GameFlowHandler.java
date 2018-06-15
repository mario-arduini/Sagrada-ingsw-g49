package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.toolcards.ToolCard;
import it.polimi.ingsw.network.server.ConnectionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameFlowHandler {
    private User player;
    private GameRoom gameRoom;
    private GamesHandler gamesHandler;
    private List<Schema> initialSchemas = null;
    private ToolCard activeToolCard;

    public GameFlowHandler(GamesHandler gamesHandler){
        this.player = null;
        this.gameRoom = null;
        this.gamesHandler = gamesHandler;
        this.activeToolCard = null;
    }

    public void setGame(GameRoom game) {
        this.gameRoom = game;
        initialSchemas = game.extractSchemas();
        //No player set if reconnection
        if (player != null) {
            player.notifyGameInfo(game.getToolCards(), game.getPublicGoals(), player.getPrivateGoal());
            player.notifySchemas(initialSchemas);
        }
    }

    //TODO: Shouldn't be able to choose a schema more than once
    public void chooseSchema(Integer schemaNumber) throws WindowAlreadySetException{
        player.setWindow(initialSchemas.get(schemaNumber));
    }

    public void placeDice(int row, int column, Dice dice) throws NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException {
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        gameRoom.placeDice(row, column, dice);
    }

    public void notifyDicePlaced(int row, int column, Dice dice){
        gameRoom.notifyAllDicePlaced(player.getNickname(), row, column, dice);
    }

    public void pass() throws NotYourTurnException{
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        this.activeToolCard = null;
        gameRoom.goOn();
    }

    public void checkGameReady(){
        List<Player> inGamePlayers = gameRoom.getPlayers();
        for (Player p: inGamePlayers)
            if (p.getWindow()==null)
                return;
        gameRoom.gameReady();
    }

    public void disconnected(){
        if (gameRoom == null){
            gamesHandler.waitingRoomDisconnection(player);
        }
    }

    public List<String> getPlayers(){
        return gameRoom == null ? gamesHandler.getWaitingPlayers() : gameRoom.getPlayers().stream().map(Player::getNickname).collect(Collectors.toList());
    }

    //TODO: Destroy game if players quit before choosing schema, here just as a reminder.
    public boolean login(String nickname, String password, ConnectionHandler connection) {
        this.player = gamesHandler.login(nickname, password, connection);
        if (gameRoom != null) {
            this.player.notifyGameInfo(gameRoom.getToolCards(), gameRoom.getPublicGoals(), player.getPrivateGoal());
            HashMap<String, Window> windows = new HashMap<>();
            HashMap<String, Integer> favorToken = new HashMap<>();
            gameRoom.getPlayers().forEach(p -> windows.put(p.getNickname(), p.getWindow()));
            gameRoom.getPlayers().forEach(p -> favorToken.put(p.getNickname(), p.getFavorToken()));
            this.player.notifyReconInfo(windows, favorToken, gameRoom.getRoundTrack());
            //TODO: maybe overload notifyRound..?
            this.player.notifyRound(gameRoom.getCurrentRound().getCurrentPlayer().getNickname(), gameRoom.getCurrentRound().getDraftPool(), false, null);
        }
        return this.player != null;
    }

    public void logout() {
        gamesHandler.logout(this.player.getNickname());
    }

    public void useToolCard(String cardName) throws NoSuchToolCardException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, NotWantedAdjacentDiceException, NoAdjacentDiceException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException {
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        Optional<ToolCard> fetch = (gameRoom.getToolCards()).stream().filter(card -> card.getName().equalsIgnoreCase(cardName)).findFirst();
        if (!fetch.isPresent()){
            throw new NoSuchToolCardException();
        }
        this.activeToolCard = fetch.get();
        this.activeToolCard.use(gameRoom);
        gameRoom.notifyAllToolCardUsed(player.getNickname(), activeToolCard.getName(), player.getWindow());
    }
}
