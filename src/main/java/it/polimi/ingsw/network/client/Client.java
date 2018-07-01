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

    Client(CLIHandler cliHandler) throws RemoteException {
        super();
        ClientLogger.initLogger(LOGGER);
        this.gameSnapshot = new GameSnapshot();
        this.cliHandler = cliHandler;
        gameStarted = false;
        flagContinue = false;
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
                server = serverInterface.login(nickname, password,this);
                setServerResult(true);
            } catch (RemoteException | LoginFailedException e) {
                LOGGER.warning(e.toString());
            }
        }
    }

    void setLogged(boolean logged){
        this.logged = logged;
    }

    void welcomePlayer(){
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
            server.placeDice(row - 1, column - 1, gameSnapshot.getDraftPool().get(diceNumber - 1));
        } catch (RemoteException | GameOverException | NotYourTurnException | NoAdjacentDiceException | BadAdjacentDiceException | DiceAlreadyExtractedException | FirstDiceMisplacedException | DiceNotInDraftPoolException | ConstraintViolatedException | GameNotStartedException | NoSameColorDicesException e) {
            setServerResult(false);
            LOGGER.warning(e.toString());
        }
    }

    void useToolCard(String name){
        try {
            server.useToolCard(name);
        } catch (RemoteException | GameNotStartedException | GameOverException | InvalidDiceValueException | NoSuchToolCardException | NotYourSecondTurnException | NoDiceInRoundTrackException | AlreadyDraftedException | NotEnoughFavorTokenException | InvalidFavorTokenNumberException | NotYourTurnException | NoDiceInWindowException | ConstraintViolatedException | BadAdjacentDiceException | NotWantedAdjacentDiceException | FirstDiceMisplacedException | NoAdjacentDiceException | NotDraftedYetException | NotYourFirstTurnException | NoSameColorDicesException | NothingCanBeMovedException e) {
            setServerResult(false);
            LOGGER.warning(e.toString());
        }
    }

    void logout(){
        logged = false;
        if(serverConnected) {
            try {
                server.logout();  //TODO: control behav with rmi
            } catch (RemoteException e) {
                LOGGER.warning(e.toString());
            }
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
        setServerResult(true);
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
        if(!gameStarted) {
            gameSnapshot.removeOtherPlayer(nickname);
            cliHandler.printWaitingRoom();
        }
        else{
            //TODO: notify other logout
        }
    }

    void sendSchemaChoice(int choice){
        try {
            server.chooseSchema(choice);
        } catch (GameNotStartedException | GameOverException | WindowAlreadySetException | RemoteException e) {
            LOGGER.warning(e.toString());
        }
    }

    @Override
    public void notifySchemas(List<Schema> schemas) throws RemoteException{
        gameStarted = true;
        cliHandler.printSchemaChoice(gameSnapshot, schemas);
        setServerResult(true);
    }

    @Override
    public void notifyOthersSchemas(Map<String, Schema> playersSchemas) throws RemoteException{
        for (Map.Entry<String, Schema> entry : playersSchemas.entrySet()) {
            if(!entry.getKey().equals(gameSnapshot.getPlayer().getNickname()))
                gameSnapshot.findPlayer(entry.getKey()).get().setWindow(entry.getValue());
            else
                gameSnapshot.getPlayer().setWindow(entry.getValue());
        }
        setServerResult(true);
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

        CLIHandler.printGame(gameSnapshot);
        CLIHandler.printMenu(gameSnapshot);
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

            CLIHandler.printGame(gameSnapshot);
            CLIHandler.printMenu(gameSnapshot);

            if(player.equalsIgnoreCase(gameSnapshot.getPlayer().getNickname())) {
                gameSnapshot.getPlayer().setUsedToolCard(true);
                serverResult = true;
                setServerResult(true);
            }
            else
                cliHandler.notifyUsedToolCard(player, toolCard);
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
        gameStarted = true;
        setServerResult(true);
    }

    @Override
    public void notifyEndGame(List<Score> scores) throws RemoteException{
        cliHandler.gameOver(scores);
    }











    void serverDisconnected(){
        serverConnected = false;
        if(logged) {
            cliHandler.notifyServerDisconnected();
            logged = false;
        }
    }

    GameSnapshot getGameSnapshot(){
        return gameSnapshot;
    }

    boolean isGameStarted() {
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

    void setServerResult(boolean serverResult){
        this.serverResult = serverResult;
        flagContinue = true;
        if(cliHandler.isWaiting())
            synchronized (cliHandler){
                cliHandler.notifyAll();
            }
    }

    //region TOOLCARD

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
