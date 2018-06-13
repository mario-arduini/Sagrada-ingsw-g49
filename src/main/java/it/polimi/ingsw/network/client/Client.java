package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.model.*;

import java.net.SocketException;
import java.util.List;
import java.util.logging.*;

public class Client {

    private static final Logger LOGGER = Logger.getLogger( Client.class.getName() );
    private static final String CLI_SCHEMA_ROW = "---------------------    ---------------------";
    private static final String CLI_21_DASH = "---------------------";
    private static final int ROWS_NUMBER = 4;
    private static final int COLUMNS_NUMBER = 5;

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
        if(server.login(nickname, password)){
            return true;
        }
        return false;
    }

    boolean isLogged(){
        return logged;
    }

    void setLogged(boolean logged){
        this.logged = logged;
    }

    void welcomePlayer(){
        cliHandler.welcomePlayer();
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

    int chooseSchema(){
        return cliHandler.chooseSchema();
    }

    boolean sendSchema(int choice){
        return server.sendSchema(choice);
    }

    void notifyNewTurn(String nickname, boolean newRound){
        getGameSnapshot().getPlayer().setMyTurn(nickname.equals(gameSnapshot.getPlayer().getNickname()));
        getGameSnapshot().getPlayer().setDiceExtracted(false);
        getGameSnapshot().getPlayer().setUsedToolCard(false);
        if(gameSnapshot.getPlayer().isMyTurn())
            cliHandler.notifyNewTurn(newRound);
        else
            cliHandler.notifyNewTurn(nickname, newRound);
    }

    boolean placeDice(int dice, int row, int column){
        Dice choice = gameSnapshot.getDraftPool().get(dice - 1);
        if(server.placeDice(choice, row, column)){
            gameSnapshot.getPlayer().getWindow().addDice(row - 1, column - 1, choice);
            gameSnapshot.getDraftPool().remove(dice - 1);
            gameSnapshot.getPlayer().setDiceExtracted(true);
            ClientLogger.printWithClear("");
            printGame();
            printMenu();
            verifyEndTurn();
            return true;
        }
        return false;
    }

    void setPrivateGoal(PrivateGoal privateGoal){
        gameSnapshot.getPlayer().setPrivateGoal(privateGoal);
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
        verifyEndTurn();
        return getGameSnapshot().getPlayer().isToolCardAlreadyUsed();
    }

    private void verifyEndTurn(){
        if(getGameSnapshot().getPlayer().isDiceAlreadyExtracted() && getGameSnapshot().getPlayer().isToolCardAlreadyUsed()){
            cliHandler.notifyEndTurn();
            pass();
        }
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

    //region DEPRECATED
    void addPlayers(List<String> newPlayers){
        cliHandler.printNewPlayers(newPlayers);
    }

    void removePlayer(String nickname){
        cliHandler.printLoggedOutPlayer(nickname);
    }
    //endregion

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

    void clear(){
        cliHandler.clear();
    }

    void printSchemas(List<Schema> schemas){
        Constraint constraint;
        Schema currentSchema;
        ClientLogger.println("\nSCHEMA CHOICE");
        for(int i=0;i<schemas.size();i+=2){
            ClientLogger.println("");
            ClientLogger.print((i + 1) + ") " + schemas.get(i).getName());// + "               " + (i + 2) + ") " + schemas.get(i).getName());
            for(int s = schemas.get(i).getName().length(); s < 22; s++)
                ClientLogger.print(" ");
            ClientLogger.println((i + 2) + ") " + schemas.get(i + 1).getName());
            ClientLogger.println(CLI_SCHEMA_ROW);
            for(int r=0;r<ROWS_NUMBER;r++){
                currentSchema = schemas.get(i);
                for(int c=0;c<COLUMNS_NUMBER;c++){
                    constraint = currentSchema.getConstraint(r,c);
                    if(constraint==null) ClientLogger.print("|   ");
                    else if(constraint.getColor()!=null) ClientLogger.print("| "+ constraint.getColor().escape() +"■ "+Color.RESET);
                    else if(constraint.getNumber()!=0) ClientLogger.print("| "+constraint.getNumber()+" ");
                }
                ClientLogger.print("|    ");
                currentSchema = schemas.get(i+1);
                for(int c=0;c<COLUMNS_NUMBER;c++){
                    constraint = currentSchema.getConstraint(r,c);
                    if(constraint==null) ClientLogger.print("|   ");
                    else if(constraint.getColor()!=null) ClientLogger.print("| "+ constraint.getColor().escape() +"■ "+Color.RESET);
                    else if(constraint.getNumber()!=0) ClientLogger.print("| "+constraint.getNumber()+" ");
                }
                ClientLogger.println("|");
                ClientLogger.println(CLI_SCHEMA_ROW);
            }
            ClientLogger.println("Difficulty: "+schemas.get(i).getDifficulty()+"            Difficulty: "+schemas.get(i+1).getDifficulty());
        }
    }

    void printGame(){
        printHeader();
        PlayerSnapshot p = gameSnapshot.getPlayer();
        List<PlayerSnapshot> otherPlayers = gameSnapshot.getOtherPlayers();
        int whiteSpaceNum,opNum=otherPlayers.size();
        Window currentWindow;
        Constraint constraint;

        ClientLogger.println("");
        ClientLogger.print(p.getNickname());
        whiteSpaceNum = 25 - p.getNickname().length();
        for(int i=0;i<whiteSpaceNum;i++) ClientLogger.print(" ");
        ClientLogger.print("  |");
        for(PlayerSnapshot op : otherPlayers){
            ClientLogger.print("    "+op.getNickname());
            whiteSpaceNum = 21 - op.getNickname().length();
            for(int i=0;i<whiteSpaceNum;i++) ClientLogger.print(" ");
        }
        ClientLogger.println("");

        ClientLogger.print(" ");
        for(int i = 0; i < 5; i++)
            ClientLogger.print("   " + (i + 1));
        ClientLogger.print("      |      ");
        for(int i = 0; i < 5; i++)
            ClientLogger.print("  " + (i + 1) + " ");
        ClientLogger.println("");

        ClientLogger.print("  " + CLI_21_DASH+"    |");
        for(int i=0;i<opNum;i++)
            ClientLogger.print("      "+CLI_21_DASH);
        ClientLogger.println("");

        for(int r=0;r<ROWS_NUMBER;r++){
            ClientLogger.print((r + 1) + " ");
            currentWindow = p.getWindow();
            for(int c=0;c<COLUMNS_NUMBER;c++){
                if(currentWindow.getCell(r,c)!=null) ClientLogger.print("| "+currentWindow.getCell(r,c)+" ");
                else {
                    constraint = currentWindow.getSchema().getConstraint(r,c);
                    if(constraint==null) ClientLogger.print("|   ");
                    else if(constraint.getColor()!=null) ClientLogger.print("| "+ constraint.getColor().escape() +"■ "+Color.RESET);
                    else if(constraint.getNumber()!=0) ClientLogger.print("| "+constraint.getNumber()+" ");
                }
            }
            ClientLogger.print("|    |");

            for(PlayerSnapshot op : otherPlayers){
                currentWindow = op.getWindow();
                ClientLogger.print("    " + (r + 1) + " ");
                for(int c=0;c<COLUMNS_NUMBER;c++){
                    if(currentWindow.getCell(r,c)!=null) ClientLogger.print("| "+currentWindow.getCell(r,c)+" ");
                    else {
                        constraint = currentWindow.getSchema().getConstraint(r,c);
                        if(constraint==null) ClientLogger.print("|   ");
                        else if(constraint.getColor()!=null) ClientLogger.print("| "+ constraint.getColor().escape() +"■ "+Color.RESET);
                        else if(constraint.getNumber()!=0) ClientLogger.print("| "+constraint.getNumber()+" ");
                    }
                }
                ClientLogger.print("|");
            }
            ClientLogger.println("");


            ClientLogger.print("  " + CLI_21_DASH+"    |");
            for(int i=0;i<opNum;i++)
                ClientLogger.print("      "+CLI_21_DASH);
            ClientLogger.println("");
        }

        // print draftpool
        ClientLogger.println("");
        ClientLogger.println("Draft Pool                 |    Round Track");
        for(Dice dice : gameSnapshot.getDraftPool()){
            ClientLogger.print(dice+"  ");
        }
        for(int i=gameSnapshot.getDraftPool().size();i<8;i++) ClientLogger.print("   ");
        ClientLogger.print("   |  ");
        for(Dice dice : gameSnapshot.getRoundTrack()){
            ClientLogger.print("  "+dice);
        }
        for(int i=gameSnapshot.getRoundTrack().size();i<9;i++) ClientLogger.print("  \u25A1");

        ClientLogger.println("\n\nYou have " + gameSnapshot.getPlayer().getFavorToken() + " favour token" + (gameSnapshot.getPlayer().getFavorToken() > 1 ? "s" : ""));


//        cliHandler.printMenu();
//        if(!cliHandler.getPlayingRound()) {
//            cliHandler.setPlayingRound(true);
//            synchronized (cliHandler) {
//                cliHandler.notifyAll();
//            }
//        }
    }

    void printMenu(){
        cliHandler.printMenu();
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

    void printToolCards(){
        cliHandler.printToolCards(gameSnapshot.getToolCards());
    }

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

    void printHeader(){
        cliHandler.printPublicGoals(gameSnapshot.getPublicGoals());
        cliHandler.printPrivateGoal(gameSnapshot.getPlayer().getPrivateGoal());
        cliHandler.printToolCards(gameSnapshot.getToolCards());
    }

    void notifyStartGame(){
        cliHandler.notifyStartGame();
    }

    public static void main(String[] args) {
        if(args.length != 2){
            ClientLogger.printlnWithClear((args.length < 2 ? "Not enough" : "Too many") + " parameters");
            return;
        }

        Client client = new Client();
        CLIHandler cliHandler;
        ClientLogger.LogToFile();

        if(args[0].equalsIgnoreCase("-g"))
            switch (args[1].toLowerCase()){
                case "cli":
                    cliHandler = new CLIHandler(client);
                    client.setCLIHandler(cliHandler);
                    cliHandler.start();
                    break;
                case "gui":
                    break;
                default:
                    ClientLogger.printlnWithClear("Invalid graphic choice");
                    return;
            }
        client.logout();
    }
}
