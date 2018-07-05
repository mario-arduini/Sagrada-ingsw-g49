package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.exceptions.*;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.RMIInterfaces.ClientInterface;
import it.polimi.ingsw.network.RMIInterfaces.FlowHandlerInterface;
import it.polimi.ingsw.network.RMIInterfaces.LoginInterface;
import it.polimi.ingsw.network.client.model.*;
import it.polimi.ingsw.network.server.exception.LoginFailedException;

import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.*;

public class Client extends UnicastRemoteObject implements ClientInterface {

    private static final Logger LOGGER = Logger.getLogger( Client.class.getName() );

    private String serverAddress;
    private int serverPort;
    private transient GraphicInterface handler;
    private FlowHandlerInterface server;
    public enum ConnectionType{ RMI, SOCKET }
    private boolean logged;
    private String password;
    private boolean gameStarted;
    private boolean serverConnected;
    private transient GameSnapshot gameSnapshot;
    private transient LoginInterface serverInterface;
    private ClientSocketHandler socketHandler;

    public Client(GraphicInterface handler) throws RemoteException {
        ClientLogger.initLogger(LOGGER);
        this.gameSnapshot = new GameSnapshot();
        this.handler = handler;
        gameStarted = false;
    }

    public void setServerAddress(String serverAddress){
        this.serverAddress = serverAddress;
    }

    public void setServerPort(int serverPort){
        this.serverPort = serverPort;
    }

    public void setHandler(GraphicInterface handler){
        this.handler = handler;
    }

    boolean getServerConnected(){
        return serverConnected;
    }

    public void login(String nickname, String password){
        gameSnapshot.setPlayer(nickname);
        this.password  = password;
        if(serverInterface == null)
            server = socketHandler.login(nickname, password);
        else {
            try {
                server = serverInterface.login(nickname, password,this);
                setServerResult(true);
            } catch (LoginFailedException e) {
                setServerResult(false);
            }catch (RemoteException e){
                serverDisconnected();
            }
        }
    }

    private void tryReconnection(){

    }

    void setLogged(){
        this.logged = true;
    }

    public boolean createConnection(ConnectionType connectionType) {
        if(connectionType == ConnectionType.SOCKET) {
            try {
                socketHandler = new ClientSocketHandler(this, serverAddress, serverPort);
            } catch (SocketException e) {
                return false;
            }
        }
        else if(connectionType == ConnectionType.RMI) {
            try {
                Registry registry = LocateRegistry.getRegistry(serverAddress);
                this.serverInterface = (LoginInterface) registry.lookup("logger");
            }  catch (NotBoundException | RemoteException e) {
                return false;
            }
        }
        serverConnected = true;
        setServerResult(true);
        return true;
    }

    public void placeDice(int diceNumber, int row, int column){
        try {
            server.placeDice(row - 1, column - 1, gameSnapshot.getDraftPool().get(diceNumber - 1));
        } catch (GameOverException | NotYourTurnException | NoAdjacentDiceException | BadAdjacentDiceException | DiceAlreadyExtractedException | FirstDiceMisplacedException | DiceNotInDraftPoolException | ConstraintViolatedException | GameNotStartedException | NoSameColorDicesException e) {
            setServerResult(false);
            LOGGER.warning(e.toString());
        } catch (RemoteException e){
            serverDisconnected();
        }
    }

    public void useToolCard(String name){
        try {
            server.useToolCard(name);
        } catch (GameNotStartedException | NotEnoughDiceToMoveException | GameOverException | ToolcardAlreadyUsedException | NoSuchToolCardException | NotYourSecondTurnException | NoDiceInRoundTrackException | AlreadyDraftedException | NotEnoughFavorTokenException | InvalidFavorTokenNumberException | NotYourTurnException | NoDiceInWindowException | NotDraftedYetException | NotYourFirstTurnException | NoSameColorDicesException | NothingCanBeMovedException | PlayerSuspendedException e) {
            setServerResult(false);
            LOGGER.warning(e.toString());
        } catch (RemoteException e){
            serverDisconnected();
        }
    }

    void logout(){
        logged = false;
        if(serverConnected) {
            try {
                server.logout();
            } catch (RemoteException e) {
                serverDisconnected();
            }
        }
    }

    void verifyEndTurn(){
        if(gameSnapshot.getPlayer().isDiceAlreadyExtracted() && gameSnapshot.getPlayer().isToolCardAlreadyUsed())
            pass();
    }

    public void pass(){
        try {
            server.pass();
        } catch (GameNotStartedException | GameOverException | NotYourTurnException e) {
            LOGGER.warning(e.toString());
        } catch (RemoteException e){
            serverDisconnected();
        }
    }







    @Override
    public void notifyLogin(List<String> nicknames){
        setServerResult(true);
        for(String nickname : nicknames)
            gameSnapshot.addOtherPlayer(nickname);
        handler.printWaitingRoom();
    }

    @Override
    public void notifyLogin(String nickname){
        gameSnapshot.addOtherPlayer(nickname);
        handler.printWaitingRoom();
    }

    @Override
    public void notifyLogout(String nickname){
        if(!gameStarted) {
            gameSnapshot.removeOtherPlayer(nickname);
            handler.printWaitingRoom();
        }
    }

    public void sendSchemaChoice(int choice){
        try {
            server.chooseSchema(choice);
        } catch (GameNotStartedException | GameOverException | WindowAlreadySetException e) {
            LOGGER.warning(e.toString());
        }  catch (RemoteException e){
            serverDisconnected();
        }
    }

    @Override
    public void notifySchemas(List<Schema> schemas){
        gameStarted = true;
        handler.printSchemaChoice(gameSnapshot, schemas);
        setServerResult(true);
    }

    @Override
    public void notifyOthersSchemas(Map<String, Schema> playersSchemas){
        for (Map.Entry<String, Schema> entry : playersSchemas.entrySet()) {
            if(!entry.getKey().equals(gameSnapshot.getPlayer().getNickname()))
                gameSnapshot.findPlayer(entry.getKey()).ifPresent(playerSnapshot -> playerSnapshot.setWindow(entry.getValue()));
            else
                gameSnapshot.getPlayer().setWindow(entry.getValue());
        }
        setServerResult(true);
    }

    @Override
    public void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack){
        gameSnapshot.getPlayer().setMyTurn(currentPlayer.equals(gameSnapshot.getPlayer().getNickname()));
        gameSnapshot.getPlayer().setDiceExtracted(false);
        gameSnapshot.getPlayer().setUsedToolCard(false);
        gameSnapshot.setCurrentPlayer(currentPlayer);
        gameSnapshot.setDraftPool(draftPool);
        if(newRound)
            gameSnapshot.setRoundTrack(roundTrack);

        handler.printGame(gameSnapshot);
        handler.printMenu(gameSnapshot);
    }

    @Override
    public void notifyDicePlaced(String nickname, int row, int column, Dice dice){
        gameSnapshot.getDraftPool().remove(dice);
        gameSnapshot.findPlayer(nickname).ifPresent(playerSnapshot -> playerSnapshot.getWindow().setDice(row, column, dice));
        if(nickname.equals(gameSnapshot.getPlayer().getNickname())){
            gameSnapshot.getPlayer().setDiceExtracted(true);
            setServerResult(true);
        }
        handler.printGame(gameSnapshot);
        handler.printMenu(gameSnapshot);
    }

    @Override
    public void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack){
        Optional<PlayerSnapshot> playerSnapshot = gameSnapshot.findPlayer(player);
        ToolCard toolCardUsed = gameSnapshot.getToolCardByName(toolCard);
        if(playerSnapshot.isPresent()){
            playerSnapshot.get().useFavorToken(toolCardUsed.getUsed() ? 2 : 1);
            toolCardUsed.setUsed();
            playerSnapshot.get().setWindow(window);
            gameSnapshot.setRoundTrack(roundTrack);
            gameSnapshot.setDraftPool(draftPool);

            if(player.equalsIgnoreCase(gameSnapshot.getPlayer().getNickname())) {
                gameSnapshot.getPlayer().setUsedToolCard(true);
                setServerResult(true);
            }
            else
                handler.notifyUsedToolCard(player, toolCard);

            handler.printGame(gameSnapshot);
            handler.printMenu(gameSnapshot);
        }
    }

    @Override
    public void notifyGameInfo(List<String> toolCards, List<String> publicGoals, String privateGoal){
        List<ToolCard> toolCardsClass = new ArrayList<>();
        for(String name : toolCards)
            toolCardsClass.add(new ToolCard(name, ""));
        gameSnapshot.setToolCards(toolCardsClass);

        gameSnapshot.setPublicGoals(publicGoals);
        gameSnapshot.getPlayer().setPrivateGoal(privateGoal);
    }

    @Override
    public void notifyReconInfo(Map<String, Window> windows, Map<String, Integer> favorToken, List<Dice> roundTrack){
        PlayerSnapshot playerSnapshot;
        for(Map.Entry<String, Window> user : windows.entrySet()){
            playerSnapshot = new PlayerSnapshot(user.getKey());
            playerSnapshot.setWindow(user.getValue());
            playerSnapshot.setFavorToken(favorToken.get(user.getKey()));
            if(user.getKey().equals(gameSnapshot.getPlayer().getNickname()))
                gameSnapshot.setPlayer(playerSnapshot);
            else
                gameSnapshot.addOtherPlayer(playerSnapshot);
        }
        gameSnapshot.setRoundTrack(roundTrack);
        gameStarted = true;
        setServerResult(true);
    }

    @Override
    public void notifyEndGame(List<Score> scores){
        handler.gameOver(scores);
        gameStarted = false;
    }

    @Override
    public void notifySuspension(String nickname){
        gameSnapshot.findPlayer(nickname).ifPresent(PlayerSnapshot::suspend);
        if(nickname.equalsIgnoreCase(gameSnapshot.getPlayer().getNickname()))
            handler.interruptInput();
        handler.wakeUp(false);
    }

    @Override
    public void showDice(Dice dice){
        handler.printDice(dice);
    }

    void newGame(){
        try {
            gameSnapshot.newGame();
            server.newGame();
        } catch (RemoteException e){
            serverDisconnected();
        }
    }







    void serverDisconnected(){
        serverConnected = false;
        if(logged) {
            handler.notifyServerDisconnected();
            logged = false;
        }
    }

    public GameSnapshot getGameSnapshot(){
        return gameSnapshot;
    }

    boolean isGameStarted() {
        return gameStarted;
    }

    void setServerResult(boolean serverResult){
        handler.wakeUp(serverResult);
    }

    //region TOOLCARD

    @Override
    public boolean askIfPlus(String prompt, boolean rollback) throws RollbackException{
        return handler.askIfPlus(prompt, rollback);
    }

    @Override
    public Dice askDiceDraftPool(String prompt, boolean rollback) throws RollbackException{
        return handler.askDiceDraftPool(prompt, rollback);
    }

    @Override
    public int askDiceRoundTrack(String prompt, boolean rollback) throws RollbackException{
        return handler.askDiceRoundTrack(prompt, rollback);
    }

    @Override
    public Coordinate askDiceWindow(String prompt, boolean rollback) throws RollbackException{
        return handler.askDiceWindow(prompt, rollback);
    }

    @Override
    public int askDiceValue(String prompt, boolean rollback) throws RollbackException{
        return handler.askDiceValue(prompt, rollback);
    }

    @Override
    public int askMoveNumber(String prompt, int n, boolean rollback) throws RollbackException{
       return handler.askMoveNumber(prompt, n, rollback);
    }
    //endregion
}
