package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.exceptions.GameNotStartedException;
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

public class GameFlowHandler {
    private Player player;
    private GameRoom gameRoom;
    private GamesHandler gamesHandler;
    private List<Schema> initialSchemas = null;
    private ToolCard activeToolCard;
    private ConnectionHandler connection;

    GameFlowHandler(GamesHandler gamesHandler, ConnectionHandler connection, Player player){
        this.player = player;
        this.gameRoom = null;
        this.gamesHandler = gamesHandler;
        this.activeToolCard = null;
        this.connection = connection;
    }

    public GameFlowHandler(GameFlowHandler gameFlow){
        this.player = gameFlow.player;
        this.gameRoom = gameFlow.gameRoom;
        this.gamesHandler = gameFlow.gamesHandler;
        this.initialSchemas = gameFlow.initialSchemas;
        this.activeToolCard = gameFlow.activeToolCard;
        this.connection = gameFlow.connection;
    }

    public Player getPlayer(){
        return this.player;
    }

    protected ConnectionHandler getConnection(){
        return this.connection;
    }

    public void setGame(GameRoom game) {
        this.gameRoom = game;
        initialSchemas = game.extractSchemas();
        connection.notifyGameInfo(game.getToolCards(), game.getPublicGoals(), player.getPrivateGoal());
        connection.notifySchemas(initialSchemas);
    }

    public void chooseSchema(Integer schemaNumber) throws WindowAlreadySetException{
        player.setWindow(initialSchemas.get(schemaNumber));
    }

    public void placeDice(int row, int column, Dice dice) throws GameNotStartedException, NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException {
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
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

    //TODO: REMOVE, remove message verified, make the method private and let it be called by choose-schema
    public void checkGameReady(){
        List<Player> inGamePlayers = gameRoom.getPlayers();
        for (Player p: inGamePlayers)
            if (p.getWindow()==null)
                return;
        gameRoom.gameReady();
    }

    public void disconnected(){
        if (gameRoom == null){
            gamesHandler.waitingRoomDisconnection(this);
        }
    }

    public List<String> getPlayers(){
        return gameRoom == null ? gamesHandler.getWaitingPlayers() : gameRoom.getPlayersNick();
    }

    public void reconnection(ConnectionHandler connection){
        gameRoom.replaceConnection(this.connection, connection);
        this.connection = connection;

        HashMap<String, Window> windows = new HashMap<>();
        HashMap<String, Integer> favorToken = new HashMap<>();
        gameRoom.getPlayers().forEach(p -> windows.put(p.getNickname(), p.getWindow()));
        gameRoom.getPlayers().forEach(p -> favorToken.put(p.getNickname(), p.getFavorToken()));

        connection.notifyGameInfo(gameRoom.getToolCards(), gameRoom.getPublicGoals(), player.getPrivateGoal());
        connection.notifyReconInfo(windows, favorToken, gameRoom.getRoundTrack());
        //TODO: maybe overload notifyRound..? Find better way? Will see it with RMI
        connection.notifyRound(gameRoom.getCurrentRound().getCurrentPlayer().getNickname(), gameRoom.getCurrentRound().getDraftPool(), false, null);

    }

    public void logout() {
        gamesHandler.logout(this.player.getNickname());
        gameRoom.logout(player.getNickname(), connection);
    }

    public void useToolCard(String cardName) throws NoSuchToolCardException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, NotWantedAdjacentDiceException, NoAdjacentDiceException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException {
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        Optional<ToolCard> fetch = (gameRoom.getToolCards()).stream().filter(card -> card.getName().equalsIgnoreCase(cardName)).findFirst();
        if (!fetch.isPresent()){
            throw new NoSuchToolCardException();
        }
        this.activeToolCard = fetch.get();
        this.activeToolCard.use(gameRoom, connection);
        gameRoom.notifyAllToolCardUsed(player.getNickname(), activeToolCard.getName(), player.getWindow());
    }
}
