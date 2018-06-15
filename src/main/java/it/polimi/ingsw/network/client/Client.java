package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.model.*;

import java.net.SocketException;
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
    private boolean serverConnected;    //* is it useful?
    private GameSnapshot gameSnapshot;

    private Client(){
        super();
        this.gameSnapshot = new GameSnapshot();
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
            server = null;
            return false;
        }
        return true;
    }

    int chooseSchema(List<Schema> schemas){
        return cliHandler.chooseSchema(gameSnapshot, schemas);
    }

    boolean sendSchema(int choice){
        return server.sendSchema(choice);
    }

    void notifyNewTurn(String nickname){
        getGameSnapshot().getPlayer().setMyTurn(nickname.equals(gameSnapshot.getPlayer().getNickname()));
        getGameSnapshot().getPlayer().setDiceExtracted(false);
        getGameSnapshot().getPlayer().setUsedToolCard(false);
        gameSnapshot.setCurrentPlayer(nickname);
    }

    boolean placeDice(int diceNumber, int row, int column){
        Dice dice = gameSnapshot.getDraftPool().get(diceNumber - 1);
        if(server.placeDice(dice, row, column)){
            gameSnapshot.getPlayer().getWindow().addDice(row - 1, column - 1, dice);
            gameSnapshot.getDraftPool().remove(diceNumber - 1);
            gameSnapshot.getPlayer().setDiceExtracted(true);
            return true;
        }
        return false;
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
        if(verifyEndTurn())
            pass();
        return getGameSnapshot().getPlayer().isToolCardAlreadyUsed();
    }

    boolean verifyEndTurn(){
        return getGameSnapshot().getPlayer().isDiceAlreadyExtracted() && getGameSnapshot().getPlayer().isToolCardAlreadyUsed();
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

    //region TOOLCARD

    void notifyUsedToolCard(String player, String toolCard){
        cliHandler.notifyUsedToolCard(player, toolCard);
    }

    void getPlusMinusOption(){
        server.sendPlusMinusOption(cliHandler.askPlusMinusOption());
    }

    void getDiceFromDraftPool(){
        server.sendDiceFromDraftPool(cliHandler.askDiceFormDraftPool());
    }

    void getDiceFromRoundTrack(){
        server.sendDiceFromRoundTrack(cliHandler.askDiceFormRoundTrack());
    }

    void getDiceFromWindow(){
        server.sendDiceFromWindow(cliHandler.askDiceFormWindow());
    }

    void getPlacementPosition(){
        server.sendPlacementPosition(cliHandler.askPlacementPosition());
    }

    void getDiceValue(){
        server.sendDiceValue(cliHandler.askDiceValue());
    }

    //endregion

    public static void main(String[] args) {
        final String ERROR = "USAGE sagrada -g [cli | gui].";

        if(args.length != 2){
            ClientLogger.printlnWithClear(ERROR);
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
                    ClientLogger.printlnWithClear(ERROR);
                    return;
            }
            client.logout();
        }
        else
            ClientLogger.printlnWithClear(ERROR);
    }
}
