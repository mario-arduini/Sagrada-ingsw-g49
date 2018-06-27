package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.exceptions.GameNotStartedException;
import it.polimi.ingsw.controller.exceptions.GameOverException;
import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
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
    private CLIHandler cliHandler;
    private FlowHandlerInterface server;
    public enum ConnectionType{ RMI, SOCKET }
    private boolean logged;
    private boolean gameStarted;
    private boolean serverConnected;    //* is it useful?
    private GameSnapshot gameSnapshot;
    private LoginInterface serverInterface;
    private ClientSocketHandler socketHandler;
    private boolean flagContinue;
    private boolean serverResult;

    Client() throws RemoteException {
        super();
        ClientLogger.initLogger(LOGGER);
        this.gameSnapshot = new GameSnapshot();
        gameStarted = false;
        flagContinue = false;
    }

    void setCLIHandler(CLIHandler cliHandler){
        this.cliHandler = cliHandler;
    }

    void setServerAddress(String serverAddress){
        this.serverAddress = serverAddress;
    }

    void setServerPort(int serverPort){
        this.serverPort = serverPort;
    }

    boolean getServerConnected(){
        return serverConnected;
    }

    void login(String nickname, String password){
        gameSnapshot.setPlayer(nickname);
        if(serverInterface == null)
            server = socketHandler.login(nickname, password);
        else {
            try {
                server = (FlowHandlerInterface) serverInterface.login(nickname, password,this);
            } catch (RemoteException | LoginFailedException e) {
                LOGGER.warning(e.toString());
            }
        }
    }

    void setLogged(boolean logged){
        this.logged = logged;
    }

    @Override
    public void welcomePlayer(){
        serverConnected = true;
        synchronized (cliHandler) {
            cliHandler.notifyAll();
        }
    }

    boolean createConnection(ConnectionType connectionType) {
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
                serverConnected = true;
                synchronized (cliHandler){
                    cliHandler.notifyAll();
                }
            }  catch (NotBoundException | RemoteException e) {
                return false;
            }
        }
        return true;
    }

    void placeDice(int diceNumber, int row, int column){
        try {
            server.placeDice(row, column, gameSnapshot.getDraftPool().get(diceNumber - 1));
        } catch (RemoteException | GameOverException | NotYourTurnException | NoAdjacentDiceException | BadAdjacentDiceException | DiceAlreadyExtractedException | FirstDiceMisplacedException | DiceNotInDraftPoolException | ConstraintViolatedException | GameNotStartedException | NoSameColorDicesException e) {
            LOGGER.warning(e.toString());
        }
    }

    void useToolCard(String name){
        try {
            server.useToolCard(name);
        } catch (RemoteException | GameNotStartedException | GameOverException | InvalidDiceValueException | NoSuchToolCardException | NotYourSecondTurnException | NoDiceInRoundTrackException | AlreadyDraftedException | NotEnoughFavorTokenException | InvalidFavorTokenNumberException | NotYourTurnException | NoDiceInWindowException | ConstraintViolatedException | BadAdjacentDiceException | NotWantedAdjacentDiceException | FirstDiceMisplacedException | NoAdjacentDiceException | NotDraftedYetException | NotYourFirstTurnException | NoSameColorDicesException | NothingCanBeMovedException e) {
            LOGGER.warning(e.toString());
        }
    }

    synchronized void logout(){
        if(serverConnected) {
            try {
                server.logout();
            } catch (RemoteException e) {
                LOGGER.warning(e.toString());
            }
        }

        logged = false;
        synchronized (cliHandler){
            notifyAll();
        }
    }

    void verifyEndTurn(){
        if(gameSnapshot.getPlayer().isDiceAlreadyExtracted() && gameSnapshot.getPlayer().isToolCardAlreadyUsed())
            pass();
    }

    void pass(){
        try {
            server.pass();
        } catch (RemoteException | GameNotStartedException | GameOverException | NotYourTurnException e) {
            LOGGER.warning(e.toString());
        }
    }







    @Override
    public void notifyLogin(List<String> nicknames) throws RemoteException{
        for(String nickname : nicknames)
            gameSnapshot.addOtherPlayer(nickname);
        cliHandler.printWaitingRoom();
    }

    @Override
    public void notifyLogin(String nickname) throws RemoteException{
        gameSnapshot.addOtherPlayer(nickname);
        cliHandler.printWaitingRoom();
    }

    @Override
    public void notifyLogout(String nickname) throws RemoteException{
        gameSnapshot.removeOtherPlayer(nickname);
        cliHandler.printWaitingRoom();
    }

    @Override
    public void notifySchemas(List<Schema> schemas) throws RemoteException{
        gameStarted = true;
        try {
            server.chooseSchema(cliHandler.chooseSchema(gameSnapshot, schemas));
        } catch (GameNotStartedException | GameOverException | WindowAlreadySetException | RemoteException e) {
            LOGGER.warning(e.toString());
        }
    }

    @Override
    public void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack) throws RemoteException{
        gameSnapshot.getPlayer().setMyTurn(currentPlayer.equals(gameSnapshot.getPlayer().getNickname()));
        gameSnapshot.getPlayer().setDiceExtracted(false);
        gameSnapshot.getPlayer().setUsedToolCard(false);
        gameSnapshot.setCurrentPlayer(currentPlayer);
        gameSnapshot.setDraftPool(draftPool);
        if(newRound)
            gameSnapshot.setRoundTrack(roundTrack);

        if(!cliHandler.getPlayingRound()) {
            cliHandler.setPlayingRound(true);
            synchronized (cliHandler) {
                cliHandler.notifyAll();
            }
        }

        CLIHandler.printGame(gameSnapshot);
        CLIHandler.printMenu(gameSnapshot);
    }

    @Override
    public void notifyOthersSchemas(Map<String, Schema> playersSchemas) throws RemoteException{
        for (Map.Entry<String, Schema> entry : playersSchemas.entrySet()) {
            if(!entry.getKey().equals(gameSnapshot.getPlayer().getNickname()))
                gameSnapshot.findPlayer(entry.getKey()).get().setWindow(entry.getValue());
            else
                gameSnapshot.getPlayer().setWindow(entry.getValue());
        }
    }

    @Override
    public void notifyDicePlaced(String nickname, int row, int column, Dice dice) throws RemoteException{
        gameSnapshot.getDraftPool().remove(dice);
        if(!nickname.equals(gameSnapshot.getPlayer().getNickname())) {
            try {
                gameSnapshot.findPlayer(nickname).get().getWindow().addDice(row, column, dice);
            } catch (ConstraintViolatedException e) {
                e.printStackTrace();
            } catch (FirstDiceMisplacedException e) {
                e.printStackTrace();
            } catch (NoAdjacentDiceException e) {
                e.printStackTrace();
            } catch (BadAdjacentDiceException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                gameSnapshot.getPlayer().getWindow().addDice(row, column, dice);
            } catch (ConstraintViolatedException e) {
                e.printStackTrace();
            } catch (FirstDiceMisplacedException e) {
                e.printStackTrace();
            } catch (NoAdjacentDiceException e) {
                e.printStackTrace();
            } catch (BadAdjacentDiceException e) {
                e.printStackTrace();
            }
            gameSnapshot.getPlayer().setDiceExtracted(true);
            setServerResult(true);
        }
        CLIHandler.printGame(gameSnapshot);
        CLIHandler.printMenu(gameSnapshot);
    }

    @Override
    public void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack) throws RemoteException{
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
                serverResult = true;
                setServerResult(true);
            }
            CLIHandler.printGame(gameSnapshot);
            CLIHandler.printMenu(gameSnapshot);
        }
    }

    @Override
    public void notifyGameInfo(List<String> toolCards, List<String> publicGoals, String privateGoal) throws RemoteException{
        List<ToolCard> toolCardsClass = new ArrayList<>();
        for(String name : toolCards)
            toolCardsClass.add(new ToolCard(name, ""));
        gameSnapshot.setToolCards(toolCardsClass);

        gameSnapshot.setPublicGoals(publicGoals);
        gameSnapshot.getPlayer().setPrivateGoal(privateGoal);
    }

    @Override
    public void notifyReconInfo(HashMap<String, Window> windows, HashMap<String, Integer> favorToken, List<Dice> roundTrack) throws RemoteException{
        PlayerSnapshot playerSnapshot;
        for(String user : windows.keySet()){
            playerSnapshot = new PlayerSnapshot(user);
            playerSnapshot.setWindow(windows.get(user));
            playerSnapshot.setFavorToken(favorToken.get(user));
            if(user.equals(gameSnapshot.getPlayer().getNickname()))
                gameSnapshot.setPlayer(playerSnapshot);
            else
                gameSnapshot.addOtherPlayer(playerSnapshot);
        }
        gameSnapshot.setRoundTrack(roundTrack);
    }

    @Override
    public void notifyEndGame(List<Score> scores) throws RemoteException{
        cliHandler.gameOver(scores);
    }












    @Override
    public synchronized void serverDisconnected(){
        if(logged) {
            cliHandler.notifyServerDisconnected();
            logged = false;
            serverConnected = false;
            synchronized (cliHandler){
                notifyAll();
            }
        }
    }

    void printMenu(){
        CLIHandler.printMenu(gameSnapshot);


        //TODO remove all below

    }

    GameSnapshot getGameSnapshot(){
        return gameSnapshot;
    }

    @Override
    public boolean isGameStarted() {
        return gameStarted;
    }

    boolean getFlagContinue(){
        return flagContinue;
    }

    void setFlagContinue(boolean flagContinue){
        this.flagContinue = flagContinue;
    }

    boolean getServerResult(){
        return serverResult;
    }

    @Override
    public void setServerResult(boolean serverResult){
        this.serverResult = serverResult;
        flagContinue = true;
        synchronized (cliHandler){
            cliHandler.notifyAll();
        }
    }

    //region TOOLCARD

    void notifyUsedToolCard(String player, String toolCard){
        cliHandler.notifyUsedToolCard(player, toolCard);
    }

    @Override
    public boolean askIfPlus(String prompt) throws RemoteException{
        return cliHandler.askIfPlus(prompt);
    }

    @Override
    public Dice askDiceDraftPool(String prompt) throws RemoteException{
        return cliHandler.askDiceDraftPool(prompt);
    }

    @Override
    public int askDiceRoundTrack(String prompt) throws RemoteException{
        return cliHandler.askDiceRoundTrack(prompt);
    }

    @Override
    public Coordinate askDiceWindow(String prompt) throws RemoteException{
        return cliHandler.askDiceWindow(prompt);
    }

    @Override
    public int askDiceValue(String prompt) throws RemoteException{
        return cliHandler.askDiceValue(prompt);
    }

    //endregion
}
