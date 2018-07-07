package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.exceptions.*;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.model.toolcards.ToolCard;
import it.polimi.ingsw.network.RMIInterfaces.ClientInterface;
import it.polimi.ingsw.network.RMIInterfaces.FlowHandlerInterface;
import it.polimi.ingsw.network.server.Logger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

public class GameFlowHandler extends UnicastRemoteObject implements FlowHandlerInterface{
    private Player player;
    private GameRoom gameRoom;
    private GamesHandler gamesHandler;
    private List<Schema> initialSchemas = null;
    private ToolCard activeToolCard;
    private ClientInterface connection;
    private boolean toolCardUsed;
    private Timer timer;
    private int secondsTimerSchema = 1000000; //TODO: read value from file.

    public GameFlowHandler(GamesHandler gamesHandler, ClientInterface connection, Player player) throws RemoteException{
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

    public void setGame(GameRoom game) {
        this.gameRoom = game;
        initialSchemas = game.extractSchemas();
        List<String> toolCards = game.getToolCards().stream().map(ToolCard::getName).collect(Collectors.toList());
        List<String> publicGoals = game.getPublicGoals().stream().map(PublicGoal::getName).collect(Collectors.toList());
        try {
            connection.notifyGameInfo(toolCards, publicGoals, player.getPrivateGoal().getName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            connection.notifySchemas(initialSchemas);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        startTimer();
    }

    public void chooseSchema(Integer schemaNumber) throws GameNotStartedException, GameOverException, WindowAlreadySetException{
        if (gameRoom == null) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (timer != null)
            timer.cancel();
        player.setWindow(initialSchemas.get(schemaNumber));
        checkGameReady();
    }

    public void placeDice(int row, int column, Dice dice) throws GameNotStartedException, GameOverException, ToolCardInUseException, NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException {
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        if (activeToolCard != null && !toolCardUsed) throw new ToolCardInUseException();

        gameRoom.placeDice(row, column, dice);
        gameRoom.notifyAllDicePlaced(player.getNickname(), row, column, dice);
    }

    public void pass() throws GameOverException, GameNotStartedException, NotYourTurnException{
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        this.activeToolCard = null;
        this.toolCardUsed = false;
        gameRoom.goOn();
    }

    private void checkGameReady(){
        if (!gameRoom.isGameStarted()) return;
        gameRoom.gameReady();
    }

    public void disconnected(){
        if (gameRoom == null)
            gamesHandler.waitingRoomDisconnection(this);
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

        Logger.print("Reconnected player " + player.getNickname());

        HashMap<String, Window> windows = new HashMap<>();
        HashMap<String, Integer> favorToken = new HashMap<>();
        gameRoom.getPlayers().forEach(p -> windows.put(p.getNickname(), p.getWindow()));
        gameRoom.getPlayers().forEach(p -> favorToken.put(p.getNickname(), p.getFavorToken()));

        List<String> toolCards = gameRoom.getToolCards().stream().map(ToolCard::getName).collect(Collectors.toList());
        List<String> publicGoals = gameRoom.getPublicGoals().stream().map(PublicGoal::getName).collect(Collectors.toList());
        try {
            connection.notifyGameInfo(toolCards, publicGoals, player.getPrivateGoal().getName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //Reconnection while using toolcard.
        //TODO: maybe overload notifyRound..? Find better way? Will see it with RMI
        if (player.getWindow() != null) {
            String toolCardName = "";
            if (activeToolCard != null && !toolCardUsed)
                toolCardName = activeToolCard.getName();
            try {
                connection.notifyReconInfo(windows, favorToken, gameRoom.getRoundTrack(), toolCardName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                connection.notifyRound(gameRoom.getCurrentRound().getCurrentPlayer().getNickname(), gameRoom.getCurrentRound().getDraftPool(), false, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else
            try {
                connection.notifyLogin(gameRoom.getPlayersNick().stream().filter(name -> !name.equalsIgnoreCase(player.getNickname())).collect(Collectors.toList()));
                connection.notifySchemas(initialSchemas);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

    }

    public void logout() {
        gamesHandler.logout(this.player.getNickname());
        gameRoom.logout(player.getNickname(), connection);
    }

    public void newGame(){
        if (gameRoom != null && !gameRoom.isGameFinished())
            gameRoom.logout(player.getNickname(), connection);
        this.gameRoom = null;
        this.player = new Player(player);
        gamesHandler.goToWaitingRoom(this);
    }

    public void useToolCard(String cardName) throws GameNotStartedException,  GameOverException, ToolCardInUseException, NoSuchToolCardException, ToolcardAlreadyUsedException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException, NotEnoughDiceToMoveException, PlayerSuspendedException {
        if (gameRoom == null || !gameRoom.getPlaying()) throw new GameNotStartedException();
        if (gameRoom.isGameFinished()) throw new GameOverException();
        if (!gameRoom.getCurrentRound().getCurrentPlayer().equals(player)) throw new NotYourTurnException();
        if (toolCardUsed) throw new ToolcardAlreadyUsedException();
        if (activeToolCard != null && !toolCardUsed) throw new ToolCardInUseException();

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

    public void continueToolCard() throws GameNotStartedException,  GameOverException, NoSuchToolCardException, ToolcardAlreadyUsedException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException, NotEnoughDiceToMoveException, PlayerSuspendedException {
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

    class TimerExpired extends TimerTask {
        public void run() {
            try {
                player.setWindow(new Schema(0, new Constraint[4][5], "Empty Schema"));
            } catch (WindowAlreadySetException | InvalidDifficultyValueException
                    | UnexpectedMatrixSizeException e) {
            }
            logout();
            try {
                connection.notifyEndGame(new ArrayList<Score>());
            } catch (RemoteException e) {
            }
        }
    }

    private void startTimer(){
        timer = new Timer();
        timer.schedule(new GameFlowHandler.TimerExpired(), (long) secondsTimerSchema * 1000);
    }

}
