package it.polimi.ingsw.server.controller;

import it.polimi.ingsw.server.controller.exceptions.*;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.model.toolcards.ToolCard;
import it.polimi.ingsw.RmiInterfaces.ClientInterface;
import it.polimi.ingsw.RmiInterfaces.FlowHandlerInterface;
import it.polimi.ingsw.server.Logger;
import it.polimi.ingsw.server.ServerConfigFile;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A GameFlowHandler handles the course of the game.
 * It is possible to create a GameFlowHandler and update its connection in order to keep alive the game status of a player.
 */
public class GameFlowHandler extends UnicastRemoteObject implements FlowHandlerInterface{
    private transient Player player;
    private transient GameRoom gameRoom;
    private transient GamesHandler gamesHandler;
    private transient List<Schema> initialSchemas = null;
    private ToolCard activeToolCard;
    private ClientInterface connection;
    private boolean toolCardUsed;
    private transient Timer timer;

    /**
     * Creates a GameFlowHandler.
     * @param gamesHandler the main handler for every game.
     * @param connection connection associated to the player.
     * @param player the player that is playing.
     * @throws RemoteException on RMI problems.
     */
    GameFlowHandler(GamesHandler gamesHandler, ClientInterface connection, Player player) throws RemoteException{
        this.player = player;
        this.gameRoom = null;
        this.gamesHandler = gamesHandler;
        this.activeToolCard = null;
        this.connection = connection;
        this.toolCardUsed = false;
    }

    public Player getPlayer(){
        return this.player;
    }

    public ClientInterface getConnection(){
        return this.connection;
    }

    /**
     * Set a game to the FlowHandler and starts procedures to notify the initial state of the game to the client.
     * @param game Game associated to the flow.
     */
    public void setGame(GameRoom game) {
        this.gameRoom = game;
        initialSchemas = game.extractSchemas();
        List<String> publicGoals = game.getPublicGoals().stream().map(PublicGoal::getName).collect(Collectors.toList());
        HashMap<String, Boolean> toolCardsMap = new HashMap<>();
        gameRoom.getToolCards().forEach(card -> toolCardsMap.put(card.getName(), card.getUsed()));

        try {
            connection.notifyGameInfo(toolCardsMap, publicGoals, player.getPrivateGoal().getName());
        } catch (RemoteException e) {
            Logger.print("Disconnection: " + player.getNickname() + e.getMessage());
        }
        try {
            connection.notifySchemas(initialSchemas);
        } catch (RemoteException e) {
            Logger.print("Disconnection: " + player.getNickname() + e.getMessage());
        }
        startTimer();
    }

    /**
     * Check if game is ready to start after choosing the schema.
     */
    private void checkGameReady(){
        if (!gameRoom.isGameStarted()) return;
        gameRoom.gameReady();
    }

    /**
     * Handles disconnection in case the player is in a waiting room.
     */
    public void disconnected(){
        if (gameRoom == null)
            gamesHandler.waitingRoomDisconnection(this);
    }

    /**
     * Returns a list containing the initial schemas to choose from.
     * @return List of Schemas if game started, none otherwise.
     */
    public List<Schema> getInitialSchemas(){
        return this.initialSchemas;
    }

    /**
     * Returns a list of player in the same lobby.
     * @return List of Strings containing the name of each player in the lobby.
     */
    public List<String> getPlayers(){
        return gameRoom == null ? gamesHandler.getWaitingPlayers() : gameRoom.getPlayersNick();
    }

    /**
     * Updates the connection to a new connection and bring the player to the state he left the game through a series of notify.
     * @param connection new connection.
     */
    public void reconnection(ClientInterface connection){
        gameRoom.replaceConnection(this.connection, connection);
        this.connection = connection;

        Logger.print("Reconnected player " + player.getNickname());

        HashMap<String, Window> windows = new HashMap<>();
        HashMap<String, Integer> favorToken = new HashMap<>();
        gameRoom.getPlayers().forEach(p -> windows.put(p.getNickname(), p.getWindow()));
        gameRoom.getPlayers().forEach(p -> favorToken.put(p.getNickname(), p.getFavorToken()));

        List<String> publicGoals = gameRoom.getPublicGoals().stream().map(PublicGoal::getName).collect(Collectors.toList());
        HashMap<String, Boolean> toolCardsMap = new HashMap<>();
        gameRoom.getToolCards().forEach(card -> toolCardsMap.put(card.getName(), card.getUsed()));
        try {
            connection.notifyGameInfo(toolCardsMap, publicGoals, player.getPrivateGoal().getName());
        } catch (RemoteException e) {
            Logger.print("Disconnection: " + player.getNickname() + e.getMessage());
        }

        if (player.getWindow() != null) {
            String toolCardName = "";
            if (activeToolCard != null && !toolCardUsed)
                toolCardName = activeToolCard.getName();
            try {
                connection.notifyReconInfo(windows, favorToken, gameRoom.getRoundTrack(), toolCardName);
            } catch (RemoteException e) {
                Logger.print("Disconnection: " + player.getNickname() + e.getMessage());
            }
            try {
                connection.notifyRound(gameRoom.getCurrentRound().getCurrentPlayer().getNickname(), gameRoom.getCurrentRound().getDraftPool(), false, null);
            } catch (RemoteException e) {
                Logger.print("Disconnection: " + player.getNickname() + e.getMessage());
            }
        }
        else
            try {
                connection.notifyLogin(gameRoom.getPlayersNick().stream().filter(name -> !name.equalsIgnoreCase(player.getNickname())).collect(Collectors.toList()));
                connection.notifySchemas(initialSchemas);
            } catch (RemoteException e) {
                Logger.print("Disconnection: " + player.getNickname() + e.getMessage());
            }

    }

    /**
     * Timer Class.
     */
    class TimerExpired extends TimerTask {
        public void run() {
            try {
                player.setWindow(new Schema(0, new Constraint[4][5], "Empty Schema"));
            } catch (WindowAlreadySetException | InvalidDifficultyValueException
                    | UnexpectedMatrixSizeException e) {
            }
            logout();
            try {
                connection.notifyEndGame(new ArrayList<>());
            } catch (RemoteException e) {
                Logger.print("Disconnection: " + player.getNickname() + e.getMessage());
            }
        }
    }

    /**
     * Starts the timer for the schema choice.
     * To be cancelled if player chooses a schema.
     */
    private void startTimer(){
        timer = new Timer();
        timer.schedule(new GameFlowHandler.TimerExpired(), (long) ServerConfigFile.getSecondsTimerSchema() * 1000);
    }

    @Override
    public void chooseSchema(Integer schemaNumber) throws GameNotStartedException, GameOverException, WindowAlreadySetException{
        if (gameRoom == null) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (timer != null)
            timer.cancel();
        player.setWindow(initialSchemas.get(schemaNumber));
        checkGameReady();
    }

    @Override
    public void placeDice(int row, int column, Dice dice) throws GameNotStartedException, GameOverException, ToolCardInUseException, NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException, DiceAlreadyHereException{
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        if (activeToolCard != null && !toolCardUsed) throw new ToolCardInUseException();

        gameRoom.placeDice(row, column, dice);
        gameRoom.notifyAllDicePlaced(player.getNickname(), row, column, dice);
    }

    @Override
    public void pass() throws GameOverException, GameNotStartedException, NotYourTurnException{
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        this.activeToolCard = null;
        this.toolCardUsed = false;
        gameRoom.goOn();
    }

    @Override
    public void logout() {
        gamesHandler.logout(this.player.getNickname());
        gameRoom.logout(player.getNickname(), connection);
    }

    @Override
    public void newGame(){
        if (gameRoom != null && !gameRoom.isGameFinished())
            gameRoom.logout(player.getNickname(), connection);
        this.gameRoom = null;
        this.initialSchemas = null;
        this.player = new Player(player);
        gamesHandler.goToWaitingRoom(this);
    }

    @Override
    public void useToolCard(String cardName) throws GameNotStartedException,  GameOverException, ToolCardInUseException, NoSuchToolCardException, ToolcardAlreadyUsedException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException, NotEnoughDiceToMoveException, PlayerSuspendedException {
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        if (toolCardUsed) throw new ToolcardAlreadyUsedException();
        if (activeToolCard != null) throw new ToolCardInUseException();

        Optional<ToolCard> fetch = (gameRoom.getToolCards()).stream().filter(card -> card.getName().equalsIgnoreCase(cardName)).findFirst();

        if (!fetch.isPresent()){
            throw new NoSuchToolCardException();
        }
        this.activeToolCard = fetch.get();

        try {
            Logger.print("Player " + player.getNickname() +" is using Toolcard " +activeToolCard.getName());
            this.activeToolCard.use(gameRoom, connection);
            this.toolCardUsed = true;
            Logger.print("Player " + player.getNickname() +" successfully used Toolcard " +activeToolCard.getName());
            gameRoom.notifyAllToolCardUsed(player.getNickname(), activeToolCard.getName(), player.getWindow());
        } catch (RollbackException e) {
            activeToolCard = null;
            toolCardUsed = false;
            Logger.print("Player " + player.getNickname() +" rollback on Toolcard " + cardName);
        }catch (DisconnectionException e){
            Logger.print("Disconnection: " + player.getNickname() + " while using " + cardName);
        }catch (Exception e){
            activeToolCard = null;
            toolCardUsed = false;
            throw e;
        }
    }

    @Override
    public void continueToolCard() throws GameNotStartedException,  GameOverException, NoSuchToolCardException, ToolcardAlreadyUsedException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NotYourTurnException, PlayerSuspendedException {
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        if (toolCardUsed) throw new ToolcardAlreadyUsedException();
        if (activeToolCard == null) throw new NoSuchToolCardException();

        String cardName = activeToolCard.getName();
        try {
            Logger.print("Player " + player.getNickname() +" is continuing to use Toolcard " + cardName);
            this.activeToolCard.continueToolCard(connection);
            this.toolCardUsed = true;
            Logger.print("Player " + player.getNickname() +" successfully used Toolcard " + cardName);
            gameRoom.notifyAllToolCardUsed(player.getNickname(), activeToolCard.getName(), player.getWindow());
        } catch (RollbackException e) {
            activeToolCard = null;
            toolCardUsed = false;
            Logger.print("Player " + player.getNickname() +" rollback on Toolcard " );
        }catch (DisconnectionException e){
            Logger.print("Disconnection: " + player.getNickname() + " while using " + cardName);
        }catch (Exception e){
            activeToolCard = null;
            toolCardUsed = false;
            throw e;
        }

    }

}
