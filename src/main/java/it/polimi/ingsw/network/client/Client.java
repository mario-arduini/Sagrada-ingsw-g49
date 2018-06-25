package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.model.*;
import it.polimi.ingsw.network.server.rmi.LoginInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.*;

public class Client {

    private static final Logger LOGGER = Logger.getLogger( Client.class.getName() );

    private String serverAddress;
    private int serverPort;
    private CLIHandler cliHandler;
    private Connection server;
    enum ConnectionType{ RMI, SOCKET }
    private boolean logged;
    private boolean gameStarted;
    private boolean serverConnected;    //* is it useful?
    private GameSnapshot gameSnapshot;
    private LoginInterface serverInterface;
    private boolean flagContinue;
    private boolean serverResult;

    private Client(){
        super();
        this.gameSnapshot = new GameSnapshot();
        gameStarted = false;
        flagContinue = false;
    }

    private void setCLIHandler(CLIHandler cliHandler){
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

    boolean login(String nickname, String password){
        gameSnapshot.setPlayer(nickname);
        return server.login(nickname, password);
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
                server = new ClientSocketHandler(this, serverAddress, serverPort);
            } catch (SocketException e) {
                return false;
            }
        }
        else if(connectionType == ConnectionType.RMI) {
            try {
                startRMI();
            }  catch (NotBoundException | RemoteException e) {
                return false;
            }
        }
        return true;
    }

    private void startRMI() throws NotBoundException, RemoteException {
            new ClientRMIHandler(serverAddress);
    }

    void placeDice(int diceNumber, int row, int column){
        server.placeDice(gameSnapshot.getDraftPool().get(diceNumber - 1), row, column);
    }

    void useToolCard(String name){
        server.useToolCard(name);
    }

    synchronized void logout(){
        if(serverConnected)
            server.logout();

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
        server.pass();
    }







    void notifyLogin(List<String> nicknames){
        for(String nickname : nicknames)
            gameSnapshot.addOtherPlayer(nickname);
        cliHandler.printWaitingRoom();
    }

    void notifyLogin(String nickname){
        gameSnapshot.addOtherPlayer(nickname);
        cliHandler.printWaitingRoom();
    }

    void notifyLogout(String nickname){
        gameSnapshot.removeOtherPlayer(nickname);
        cliHandler.printWaitingRoom();
    }

    void notifySchemas(List<Schema> schemas){
        gameStarted = true;
        server.sendSchema(cliHandler.chooseSchema(gameSnapshot, schemas));
    }

    void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack){
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

    void notifyOthersSchemas(Map<String, Schema> playersSchemas){
        for (Map.Entry<String, Schema> entry : playersSchemas.entrySet()) {
            if(!entry.getKey().equals(gameSnapshot.getPlayer().getNickname()))
                gameSnapshot.findPlayer(entry.getKey()).get().setWindow(entry.getValue());
            else
                gameSnapshot.getPlayer().setWindow(entry.getValue());
        }
    }

    void notifyDicePlaced(String nickname, int row, int column, Dice dice){
        gameSnapshot.getDraftPool().remove(dice);
        if(!nickname.equals(gameSnapshot.getPlayer().getNickname()))
            gameSnapshot.findPlayer(nickname).get().getWindow().addDice(row, column, dice);
        else {
            gameSnapshot.getPlayer().getWindow().addDice(row, column, dice);
            gameSnapshot.getPlayer().setDiceExtracted(true);
            setServerResult(true);
        }
        CLIHandler.printGame(gameSnapshot);
        CLIHandler.printMenu(gameSnapshot);
    }

    void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack){
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

    void notifyGameInfo(List<String> toolCards, List<String> publicGoals, String privateGoal){
        List<ToolCard> toolCardsClass = new ArrayList<>();
        for(String name : toolCards)
            toolCardsClass.add(new ToolCard(name, ""));
        gameSnapshot.setToolCards(toolCardsClass);

        gameSnapshot.setPublicGoals(publicGoals);
        gameSnapshot.getPlayer().setPrivateGoal(privateGoal);
    }














    synchronized void serverDisconnected(){
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
        if(!cliHandler.getPlayingRound()) {
            cliHandler.setPlayingRound(true);
            synchronized (cliHandler) {
                cliHandler.notifyAll();
            }
        }
    }

    GameSnapshot getGameSnapshot(){
        return gameSnapshot;
    }

    boolean isGameStarted() {
        return gameStarted;
    }

    void gameOver(List<Score> scores){
        cliHandler.gameOver(scores);
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
        synchronized (cliHandler){
            cliHandler.notifyAll();
        }
    }

    //region TOOLCARD

    void notifyUsedToolCard(String player, String toolCard){
        cliHandler.notifyUsedToolCard(player, toolCard);
    }

    String getPlusMinusOption(String prompt){
        return cliHandler.askPlusMinusOption(prompt);
    }

    Dice getDiceFromDraftPool(String prompt){
        return cliHandler.askDiceFromDraftPool(prompt);
    }

    int getDiceFromRoundTrack(String prompt){
        return cliHandler.askDiceFromRoundTrack(prompt);
    }

    Coordinate getDiceFromWindow(String prompt){
        return cliHandler.askDiceFromWindow(prompt);
    }

    Coordinate getPlacementPosition(){
        return cliHandler.askPlacementPosition();
    }

    int getDiceValue(String prompt){
        return cliHandler.askDiceValue(prompt);
    }

    //endregion

    public static void main(String[] args) {
        final String ERROR = "usage:  sagrada  -g  [cli | gui]";

        if(args.length != 2){
            ClientLogger.println(ERROR);
            return;
        }

        Client client = new Client();
        CLIHandler cliHandler;
        ClientLogger.LogToFile();

        if(args[0].equalsIgnoreCase("-g")) {
            switch (args[1].toLowerCase()) {
                case "cli":
                    cliHandler = new CLIHandler(client);
                    client.setCLIHandler(cliHandler);
                    cliHandler.start();
                    break;
                case "gui":
                    break;
                default:
                    ClientLogger.println(ERROR);
                    return;
            }
            client.logout();
        }
        else
            ClientLogger.println(ERROR);
    }
}
