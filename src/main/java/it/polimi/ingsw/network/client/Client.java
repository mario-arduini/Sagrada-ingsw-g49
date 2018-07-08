package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.exceptions.*;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.RmiInterfaces.ClientInterface;
import it.polimi.ingsw.network.RmiInterfaces.FlowHandlerInterface;
import it.polimi.ingsw.network.RmiInterfaces.LoginInterface;
import it.polimi.ingsw.network.client.model.*;
import it.polimi.ingsw.network.server.exception.LoginFailedException;

import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.*;

/**
 * Contains technical information about the state of client program
 * Provides methods to communicates with the server and the other way around
 */
public class Client extends UnicastRemoteObject implements ClientInterface {

    private static final Logger LOGGER = Logger.getLogger( Client.class.getName() );
    private static final int NUMBER_RECONNECTION_ATTEMPTS = 20;

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
    private boolean reconnectionInsideToolCard;

    /**
     * Creates a new Client object and initialises some parameters like the graphic handler chosen
     * @param handler the object that represents either the CLI or the GUI
     * @throws RemoteException RMI exception
     */
    public Client(GraphicInterface handler) throws RemoteException {
        ClientLogger.initLogger(LOGGER);
        this.gameSnapshot = new GameSnapshot();
        this.handler = handler;
        gameStarted = false;
        reconnectionInsideToolCard = false;
        logged = true;
    }

    /**
     * Sets the IP address of the server for the connection
     * @param serverAddress the server IP address
     */
    public void setServerAddress(String serverAddress){
        this.serverAddress = serverAddress;
    }

    /**
     * Sets the port of the server for the connection
     * @param serverPort the server port
     */
    public void setServerPort(int serverPort){
        this.serverPort = serverPort;
    }

    /**
     * Sets Sets the object that represents either the CLI or the GUI based on what the user chose
     * @param handler the object representing the type of graphic the user chose
     */
    public void setHandler(GraphicInterface handler){
        this.handler = handler;
    }

    /**
     * Used to know if the connection with the server is active
     * @return true if the connection with the server is active, false otherwise
     */
    boolean getServerConnected(){
        return serverConnected;
    }

    /**
     * Sends to the server a request to log in the user
     * @param nickname the name chosen by the user
     * @param password the password chosen by the user
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    public void login(String nickname, String password) throws ServerReconnectedException{
        gameSnapshot.setPlayer(nickname);
        this.password  = password;
        if(serverInterface == null)
            server = socketHandler.login(nickname, password);
        else {
            try {
                server = serverInterface.login(nickname, password,this);
            } catch (LoginFailedException e) {
                setServerResult(false);
            }catch (RemoteException e){
                serverDisconnected();
            }
        }
    }

    /**
     * Tries to reconnect to the server, if the connection went down, with the information already provided by the user
     * @return true if the reconnection was successful, false otherwise
     */
    private boolean tryReconnection(){
        if(serverInterface == null)
            return createConnection(ConnectionType.SOCKET);
        return createConnection(ConnectionType.RMI);
    }

    /**
     * Tries to connect to the server with the information provided by the user
     * @param connectionType the type of connection (RMI or socket) the user chose
     * @return true if the connection was successful, false otherwise
     */
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

    /**
     * Sends to the server a request to place a dice in a specific cell
     * @param diceNumber the dice chosen by the user to place in the window
     * @param row the row of the cell chosen by the user
     * @param column the column of the cell chosen by the user
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    public void placeDice(int diceNumber, int row, int column) throws ServerReconnectedException{
        try {
            server.placeDice(row - 1, column - 1, gameSnapshot.getDraftPool().get(diceNumber - 1));
        } catch (GameOverException | ToolCardInUseException | NotYourTurnException | NoAdjacentDiceException | BadAdjacentDiceException | DiceAlreadyExtractedException | FirstDiceMisplacedException | DiceNotInDraftPoolException | ConstraintViolatedException | GameNotStartedException | NoSameColorDicesException | DiceAlreadyHereException e) {
            setServerResult(false);
            LOGGER.warning(e.toString());
        } catch (RemoteException e){
            serverDisconnected();
        }
    }

    /**
     * Sends to the server a request to use a specific tool card
     * @param name the name of the tool card chosen by the user
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    public void useToolCard(String name) throws ServerReconnectedException{
        AtomicReference<Boolean> reconnection = new AtomicReference<>(false);
        new Thread(()->{
            try {
                server.useToolCard(name);
            } catch (GameNotStartedException | ToolCardInUseException | NotEnoughDiceToMoveException | GameOverException | ToolcardAlreadyUsedException | NoSuchToolCardException | NotYourSecondTurnException | NoDiceInRoundTrackException | AlreadyDraftedException | NotEnoughFavorTokenException | InvalidFavorTokenNumberException | NotYourTurnException | NoDiceInWindowException | NotDraftedYetException | NotYourFirstTurnException | NoSameColorDicesException | NothingCanBeMovedException | PlayerSuspendedException e) {
                setServerResult(false);
                LOGGER.warning(e.toString());
            } catch (RemoteException e){
                try {
                    serverDisconnected();
                } catch (ServerReconnectedException e1) {
                    reconnection.getAndSet(true);
                }
            }
        }).start();
        if(reconnection.get())
            throw new ServerReconnectedException();
    }

    /**
     * Tells the server that the user decided to log out from the game
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    public void logout() throws ServerReconnectedException{
        logged = false;
        if(serverConnected) {
            try {
                server.logout();
            } catch (RemoteException e) {
                serverDisconnected();
            }
        }
    }

    /**
     * Verifies if the user cannot do anymore moves in a specific turn and if so, it passes automatically
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    public void verifyEndTurn() throws ServerReconnectedException{
        if(gameSnapshot.getPlayer().isDiceAlreadyExtracted() && gameSnapshot.getPlayer().isToolCardAlreadyUsed())
            pass();
    }

    /**
     * Tells the server that the user passes his turn
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    public void pass() throws ServerReconnectedException{
        try {
            server.pass();
        } catch (GameNotStartedException | GameOverException | NotYourTurnException e) {
            LOGGER.warning(e.toString());
        } catch (RemoteException e){
            serverDisconnected();
        }
    }


    /**
     * Adds to the waiting room a list of nicknames of new players and notifies the user
     * @param nicknames the nicknames of the new players in the waiting room
     */
    @Override
    public void notifyLogin(List<String> nicknames){
        //setServerResult(true);
        for(String nickname : nicknames)
            gameSnapshot.addOtherPlayer(nickname);
        handler.printWaitingRoom();
    }

    /**
     * Adds to the waiting room the nickname of a new players and notifies the user
     * @param nickname the nickname of the new player in the waiting room
     */
    @Override
    public void notifyLogin(String nickname){
        gameSnapshot.addOtherPlayer(nickname);
        handler.printWaitingRoom();
    }

    /**
     * Removes from the waiting room a nickname of a player and notifies the user
     * @param nickname the nickname of the player to remove from the waiting room
     */
    @Override
    public void notifyLogout(String nickname){
        if(!gameStarted) {
            gameSnapshot.removeOtherPlayer(nickname);
            handler.printWaitingRoom();
        }
    }

    /**
     * Sends to the server the number of the schema the user chose
     * @param choice the number of the schema the user chose
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    public void sendSchemaChoice(int choice) throws ServerReconnectedException{
        try {
            server.chooseSchema(choice);
        } catch (GameNotStartedException | GameOverException | WindowAlreadySetException e) {
            LOGGER.warning(e.toString());
        }  catch (RemoteException e){
            serverDisconnected();
        }
    }

    /**
     * Notifies the possible schemas the user can choose from
     * @param schemas the schemas the user can choose from
     */
    @Override
    public void notifySchemas(List<Schema> schemas){
        handler.printSchemaChoice(gameSnapshot, schemas);
        setServerResult(true);
    }

    /**
     * Notifies to the user the schemas chosen from the other users in the game
     * @param playersSchemas the users with their chosen schemas
     */
    @Override
    public void notifyOthersSchemas(Map<String, Schema> playersSchemas){
        gameStarted = true;
        for (Map.Entry<String, Schema> entry : playersSchemas.entrySet()) {
            if(!entry.getKey().equals(gameSnapshot.getPlayer().getNickname()))
                gameSnapshot.findPlayer(entry.getKey()).ifPresent(playerSnapshot -> playerSnapshot.setWindow(entry.getValue()));
            else
                gameSnapshot.getPlayer().setWindow(entry.getValue());
        }
        setServerResult(true);
    }

    /**
     * Notifies that a new turn or round has begun to the user and sets all the new parameters of the new round/turn
     * @param currentPlayer the user who is now playing
     * @param draftPool the current content of the draft pool
     * @param newRound indicates if a new round started
     * @param roundTrack if a new round started contains the new round track
     */
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
        if(!reconnectionInsideToolCard)
            handler.printMenu(gameSnapshot);
        else
            reconnectionInsideToolCard = false;
    }

    /**
     * Notifies to the user that a specif user placed a dice and where it has been placed and updates his window
     * @param nickname the users who placed the dice
     * @param row the row of the cell where the dice was placed
     * @param column the column of the cell where the dice was placed
     * @param dice the dice that has been placed
     */
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

    /**
     * Notifies to the user that a specif user used a tool card and updates some parameters about that tool card and that user
     * @param player the users who used the tool card
     * @param toolCard the tool card used by the user
     * @param window the new window of that user
     * @param draftPool the current draft pool
     * @param roundTrack the current round track
     */
    @Override
    public void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack){
        Optional<PlayerSnapshot> playerSnapshot = gameSnapshot.findPlayer(player);
        ToolCard toolCardUsed = gameSnapshot.getToolCardByName(toolCard);
        if(playerSnapshot.isPresent()){
            playerSnapshot.get().useFavorToken(toolCardUsed.getUsed() ? 2 : 1);
            toolCardUsed.setUsed();
            gameSnapshot.setRoundTrack(roundTrack);
            gameSnapshot.setDraftPool(draftPool);

            if(player.equalsIgnoreCase(gameSnapshot.getPlayer().getNickname())) {
                gameSnapshot.getPlayer().setUsedToolCard(true);
                if(window.numOfDicePlaced() != gameSnapshot.getPlayer().getWindow().numOfDicePlaced())
                    gameSnapshot.getPlayer().setDiceExtracted(true);
                gameSnapshot.getPlayer().setWindow(window);
                setServerResult(true);
            }
            else {
                playerSnapshot.get().setWindow(window);
                handler.notifyUsedToolCard(player, toolCard);
            }

            handler.printGame(gameSnapshot);
            handler.printMenu(gameSnapshot);
        }
    }

    /**
     * Sets some information about the game, like the tool cards, the public goals, and the private goal of the user
     * @param toolCards the tool cards of this game
     * @param publicGoals the public goals of this game
     * @param privateGoal the private goal of the user
     */
    @Override
    public void notifyGameInfo(List<String> toolCards, List<String> publicGoals, String privateGoal){
        List<ToolCard> toolCardsClass = new ArrayList<>();
        for(String name : toolCards)
            toolCardsClass.add(new ToolCard(name));
        gameSnapshot.setToolCards(toolCardsClass);

        gameSnapshot.setPublicGoals(publicGoals);
        gameSnapshot.getPlayer().setPrivateGoal(privateGoal);
    }

    /**
     * Sets some information about a game, which is already started, after a reconnection
     * @param windows contains every users of the game with their window
     * @param favorToken contains every users of the game with their favour token
     * @param roundTrack the current round track of the game
     * @param cardName the name of the tool card if the connection went down during its use
     */
    @Override
    public void notifyReconInfo(Map<String, Window> windows, Map<String, Integer> favorToken, List<Dice> roundTrack, String cardName){
        PlayerSnapshot playerSnapshot;
        for(Map.Entry<String, Window> user : windows.entrySet()){
            playerSnapshot = new PlayerSnapshot(user.getKey());
            playerSnapshot.setWindow(user.getValue());
            playerSnapshot.setFavorToken(favorToken.get(user.getKey()));
            if(user.getKey().equals(gameSnapshot.getPlayer().getNickname())) {
                gameSnapshot.getPlayer().setWindow(playerSnapshot.getWindow());
                gameSnapshot.getPlayer().setFavorToken(playerSnapshot.getFavorToken());
            }
            else
                gameSnapshot.addOtherPlayer(playerSnapshot);
        }
        gameSnapshot.setRoundTrack(roundTrack);
        handler.setToolCardNotCompleted(cardName);
        reconnectionInsideToolCard = !cardName.equals("");
        gameStarted = true;
        setServerResult(true);
    }

    /**
     * Notifies the user that the game is finished and notifies the scores
     * @param scores a list of users with their score
     */
    @Override
    public void notifyEndGame(List<Score> scores){
        if(gameStarted) {
            handler.gameOver(scores);
            gameStarted = false;
        }
    }

    /**
     * Notifies the user that a certain user has been suspended
     * @param nickname the user suspended
     */
    @Override
    public void notifySuspension(String nickname){
        gameSnapshot.findPlayer(nickname).ifPresent(PlayerSnapshot::suspend);
        if(nickname.equalsIgnoreCase(gameSnapshot.getPlayer().getNickname()))
            handler.interruptInput();
        handler.wakeUp(false);
    }

    /**
     * Shows to the users a specif dice
     * @param dice the dice to be shown
     */
    @Override
    public void showDice(Dice dice){
        handler.printDice(dice);
    }

    /**
     * Notifies the user that a dice has been inserted into the draft pool
     * @param dice the dice inserted into the draft pool
     */
    @Override
    public void alertDiceInDraftPool(Dice dice){
        handler.alertDiceInDraftPool(dice);
    }

    /**
     * Asks the server to start a new game
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    public void newGame() throws ServerReconnectedException{
        try {
            gameSnapshot.newGame();
            server.newGame();
        } catch (RemoteException e){
            serverDisconnected();
        }
    }


    /**
     * Asks the server to continue to use a tool card after a reconnection if the disconnection happened during the use of a tool card
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    public void continueToolCard() throws ServerReconnectedException{
        try {
            server.continueToolCard();
        } catch (GameNotStartedException | NotEnoughDiceToMoveException | GameOverException | ToolcardAlreadyUsedException | NoSuchToolCardException | NotYourSecondTurnException | NoDiceInRoundTrackException | AlreadyDraftedException | NotEnoughFavorTokenException | InvalidFavorTokenNumberException | NotYourTurnException | NoDiceInWindowException | NotDraftedYetException | NotYourFirstTurnException | NoSameColorDicesException | NothingCanBeMovedException | PlayerSuspendedException e) {
            setServerResult(false);
            LOGGER.warning(e.toString());
        } catch (RemoteException e){
            serverDisconnected();
        }
    }

    /**
     * Notifies to the user that the connection with the server went down and try to reconnect with the server
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    void serverDisconnected() throws ServerReconnectedException{
        if(logged) {
            serverConnected = false;
            handler.notifyServerDisconnected();
            handler.interruptInput();
            handler.wakeUp(false);
            int i = 0;
            while (i < NUMBER_RECONNECTION_ATTEMPTS) {
                if (tryReconnection()) {
                    serverConnected = true;
                    gameSnapshot.newGame();
                    if(password != null)
                        login(gameSnapshot.getPlayer().getNickname(), password);
                    throw new ServerReconnectedException();
                }
                try {
                    TimeUnit.SECONDS.sleep(2);
                    i++;
                } catch (InterruptedException e) {
                    LOGGER.warning(e.toString());
                }
            }
            System.exit(0);
        }
    }

    /**
     * Used to get the object that contains information about the state of the game
     * @return the object represent the state of the game
     */
    public GameSnapshot getGameSnapshot(){
        return gameSnapshot;
    }

    /**
     * Used to know if the game started
     * @return true if the game has already started, false otherwise
     */
    boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Notifies that the server has responded
     * @param serverResult is true if everything went ok, false otherwise
     */
    void setServerResult(boolean serverResult){
        handler.wakeUp(serverResult);
    }

    //region TOOLCARD

    /** Asks the user if to increment or decrement the value of the dice
     * @param prompt the code of the message to print to the user
     * @param rollback true if the use can decide not to use the tool card anymore, false otherwise
     * @return true if user wants to the increment the value of the dice, false otherwise
     * @throws RollbackException if the users doesn't want to use the tool card anymore
     */
    @Override
    public boolean askIfPlus(String prompt, boolean rollback) throws RollbackException{
        return handler.askIfPlus(prompt, rollback);
    }

    /**
     * Asks the user to chose a dice from the draft pool
     * @param prompt the code of the message to print to the user
     * @param rollback true if the use can decide not to use the tool card anymore, false otherwise
     * @return the dice the user chose from the draft pool
     * @throws RollbackException if the users doesn't want to use the tool card anymore
     */
    @Override
    public Dice askDiceDraftPool(String prompt, boolean rollback) throws RollbackException{
        return handler.askDiceDraftPool(prompt, rollback);
    }

    /**
     * Asks the user to chose a dice from the round track
     * @param prompt the code of the message to print to the user
     * @param rollback true if the use can decide not to use the tool card anymore, false otherwise
     * @return the dice the user chose from the round track
     * @throws RollbackException if the users doesn't want to use the tool card anymore
     */
    @Override
    public int askDiceRoundTrack(String prompt, boolean rollback) throws RollbackException{
        return handler.askDiceRoundTrack(prompt, rollback);
    }

    /**
     * Asks the user to choose a free cell or a occupied one from the window based con the code message
     * @param prompt the code of the message to print to the user
     * @param rollback true if the use can decide not to use the tool card anymore, false otherwise
     * @return the cell form the windows chosen by the user
     * @throws RollbackException if the users doesn't want to use the tool card anymore
     */
    @Override
    public Coordinate askDiceWindow(String prompt, boolean rollback) throws RollbackException{
        return handler.askDiceWindow(prompt, rollback);
    }

    /**
     * Asks the user a value for a dice
     * @param prompt the code of the message to print to the user
     * @param rollback true if the use can decide not to use the tool card anymore, false otherwise
     * @return the value of the dice that the user decided
     * @throws RollbackException if the users doesn't want to use the tool card anymore
     */
    @Override
    public int askDiceValue(String prompt, boolean rollback) throws RollbackException{
        return handler.askDiceValue(prompt, rollback);
    }

    /**
     * Asks the user how many dice to move
     * @param prompt the code of the message to print to the user
     * @param n the maximum number of dice the user can decide to move
     * @param rollback true if the use can decide not to use the tool card anymore, false otherwise
     * @return the number of dice the user decided to move
     * @throws RollbackException if the users doesn't want to use the tool card anymore
     */
    @Override
    public int askMoveNumber(String prompt, int n, boolean rollback) throws RollbackException{
       return handler.askMoveNumber(prompt, n, rollback);
    }
    //endregion
}
