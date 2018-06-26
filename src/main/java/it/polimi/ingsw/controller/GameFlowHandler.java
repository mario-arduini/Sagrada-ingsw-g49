package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.exceptions.GameNotStartedException;
import it.polimi.ingsw.controller.exceptions.GameOverException;
import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.model.toolcards.ToolCard;
import it.polimi.ingsw.network.server.ClientInterface;
import it.polimi.ingsw.network.server.rmi.FlowHandlerInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameFlowHandler extends UnicastRemoteObject implements FlowHandlerInterface{
    private Player player;
    private GameRoom gameRoom;
    private GamesHandler gamesHandler;
    private List<Schema> initialSchemas = null;
    private ToolCard activeToolCard;
    private ClientInterface connection;

    GameFlowHandler(GamesHandler gamesHandler, ClientInterface connection, Player player) throws RemoteException{
        this.player = player;
        this.gameRoom = null;
        this.gamesHandler = gamesHandler;
        this.activeToolCard = null;
        this.connection = connection;
    }

    public Player getPlayer(){
        return this.player;
    }

    protected ClientInterface getConnection(){
        return this.connection;
    }

    public void setGame(GameRoom game) {
        this.gameRoom = game;
        initialSchemas = game.extractSchemas();
        List<String> toolCards = game.getToolCards().stream().map(ToolCard::getName).collect(Collectors.toList());
        List<String> publicGoals = game.getPublicGoals().stream().map(PublicGoal::getName).collect(Collectors.toList());
        connection.notifyGameInfo(toolCards, publicGoals, player.getPrivateGoal().getName());
        connection.notifySchemas(initialSchemas);
    }

    public void chooseSchema(Integer schemaNumber) throws GameNotStartedException, GameOverException, WindowAlreadySetException{
        if (gameRoom == null) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        player.setWindow(initialSchemas.get(schemaNumber));
        checkGameReady();
    }

    public void placeDice(int row, int column, Dice dice) throws GameNotStartedException, GameOverException, NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException {
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        gameRoom.placeDice(row, column, dice);
        gameRoom.notifyAllDicePlaced(player.getNickname(), row, column, dice);
    }

    public void pass() throws GameOverException, GameNotStartedException, NotYourTurnException{
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        this.activeToolCard = null;
        gameRoom.goOn();
    }

    private void checkGameReady(){
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

    public List<Schema> getInitialSchemas(){
        return this.initialSchemas;
    }

    public List<String> getPlayers(){
        return gameRoom == null ? gamesHandler.getWaitingPlayers() : gameRoom.getPlayersNick();
    }

    public void reconnection(ClientInterface connection){
        gameRoom.replaceConnection(this.connection, connection);
        this.connection = connection;

        HashMap<String, Window> windows = new HashMap<>();
        HashMap<String, Integer> favorToken = new HashMap<>();
        gameRoom.getPlayers().forEach(p -> windows.put(p.getNickname(), p.getWindow()));
        gameRoom.getPlayers().forEach(p -> favorToken.put(p.getNickname(), p.getFavorToken()));

        List<String> toolCards = gameRoom.getToolCards().stream().map(ToolCard::getName).collect(Collectors.toList());
        List<String> publicGoals = gameRoom.getPublicGoals().stream().map(PublicGoal::getName).collect(Collectors.toList());
        connection.notifyGameInfo(toolCards, publicGoals, player.getPrivateGoal().getName());
        connection.notifyReconInfo(windows, favorToken, gameRoom.getRoundTrack());
        //TODO: maybe overload notifyRound..? Find better way? Will see it with RMI
        connection.notifyRound(gameRoom.getCurrentRound().getCurrentPlayer().getNickname(), gameRoom.getCurrentRound().getDraftPool(), false, null);

    }

    public void logout() {
        gamesHandler.logout(this.player.getNickname());
        gameRoom.logout(player.getNickname(), connection);
    }

    public void newGame(){
        if (gameRoom != null && !gameRoom.isGameFinished())
            gameRoom.logout(player.getNickname(), connection);
        this.gameRoom = null;
        gamesHandler.goToWaitingRoom(this);
    }

    public void useToolCard(String cardName) throws GameNotStartedException, GameOverException, NoSuchToolCardException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, NotWantedAdjacentDiceException, NoAdjacentDiceException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException {
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();

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
