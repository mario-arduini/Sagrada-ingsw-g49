package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.model.*;
import it.polimi.ingsw.network.server.rmi.LoginInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
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

    private Client(){
        super();
        this.gameSnapshot = new GameSnapshot();
        gameStarted = false;
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

    boolean createConnection(ConnectionType connectionType, GameSnapshot gameSnapshot) {
        if(connectionType == ConnectionType.SOCKET) {
            try {
                server = new ClientSocketHandler(this, serverAddress, serverPort, gameSnapshot);
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

    void chooseSchema(List<Schema> schemas){
        gameStarted = true;
        server.sendSchema(cliHandler.chooseSchema(gameSnapshot, schemas));
    }

    void notifyNewTurn(String nickname){
        getGameSnapshot().getPlayer().setMyTurn(nickname.equals(gameSnapshot.getPlayer().getNickname()));
        getGameSnapshot().getPlayer().setDiceExtracted(false);
        getGameSnapshot().getPlayer().setUsedToolCard(false);
        gameSnapshot.setCurrentPlayer(nickname);
    }

    boolean placeDice(int diceNumber, int row, int column){
        gameSnapshot.getPlayer().setDiceExtracted(server.placeDice(gameSnapshot.getDraftPool().get(diceNumber - 1), row, column));
        return getGameSnapshot().getPlayer().isDiceAlreadyExtracted();
    }

    synchronized void logout(){
        if(serverConnected)
            server.logout();

        logged = false;
        synchronized (cliHandler){
            notifyAll();
        }
    }

    boolean useToolCard(String name){
        gameSnapshot.getPlayer().setUsedToolCard(server.useToolCard(name));
        return getGameSnapshot().getPlayer().isToolCardAlreadyUsed();
    }

    void verifyEndTurn(){
        if(gameSnapshot.getPlayer().isDiceAlreadyExtracted() && gameSnapshot.getPlayer().isToolCardAlreadyUsed())
            pass();
    }

    void pass(){
        server.pass();
    }

    void updateWaitingRoom(List<String> nicknames, boolean newUsers){
        for(String nickname : nicknames)
            if (newUsers)
                gameSnapshot.addOtherPlayer(nickname);
            else
                gameSnapshot.removeOtherPlayer(nickname);

        cliHandler.printWaitingRoom();
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

    void printGame(){
        CLIHandler.printGame(gameSnapshot);
    }

    void printMenu(){
        cliHandler.printMenu();


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
