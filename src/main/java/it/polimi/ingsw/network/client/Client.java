package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.Color;

import java.util.List;
import java.util.Scanner;
import java.util.logging.*;

public class Client {

    private static final Logger LOGGER = Logger.getLogger( Client.class.getName() );
    private static final String CLI_SCHEMA_ROW = "---------------------    ---------------------";
    private static final String CLI_21_DASH = "---------------------";
    private static final int ROWS_NUMBER = 4;
    private static final int COLUMNS_NUMBER = 5;

    private String nickname;
    private String serverAddress;
    private int serverPort;
    private Connection server;
    enum ConnectionType{ RMI, SOCKET }
    private boolean logged;
    private boolean serverConnected;
    private GameSnapshot gameSnapshot;
    private boolean myTurn;
    private boolean diceExtracted;
    private boolean usedToolCard;
    private CLIHandler cliHandler;

    private Client(){
        super();
    }

    private void setCliHandler(CLIHandler cliHandler){
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
        this.nickname = nickname;
        if(server.login(nickname, password)){
            initGameSnapshot();
            return true;
        }
        return false;
    }

    String getNickname(){
        return nickname;
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

    void createConnection(ConnectionType connectionType){
        if(connectionType == ConnectionType.SOCKET)
            server = new ClientSocketHandler(this, serverAddress, serverPort);
        else if(connectionType == ConnectionType.RMI)
            server = null;
    }

    int chooseSchema(){
        return cliHandler.chooseSchema();
    }

    boolean sendSchema(int choice){
        return server.sendSchema(choice);
    }

    void notifyNewTurn(String nickname, boolean newRound){
        myTurn = nickname.equals(this.nickname);
        diceExtracted = false;
        usedToolCard = false;
        if(myTurn)
            cliHandler.notifyNewTurn(newRound);
        else
            cliHandler.notifyNewTurn(nickname, newRound);
    }

    boolean placeDice(int dice, int row, int column){
        Dice choice = gameSnapshot.getDraftPool().get(dice - 1);
        if(server.placeDice(choice, row, column)){
            gameSnapshot.getPlayer().getWindow().addDice(row - 1, column - 1, choice);
            gameSnapshot.getDraftPool().remove(dice - 1);
            diceExtracted = true;
            printGame();
            verifyEndTurn();
            return true;
        }
        return false;
    }

    void setPrivateGoal(String[] privateGoal){}

    synchronized void logout(){
        if(serverConnected)
            server.logout();

        logged = false;
        synchronized (cliHandler){
            notifyAll();
        }
    }

    boolean isMyTurn(){
        return myTurn;
    }

    boolean diceAlreadyExtracted(){
        return diceExtracted;
    }

    boolean cardToolAlreadyUsed(){
        return usedToolCard;
    }

    boolean useToolCard(String name){
        usedToolCard =  server.useToolCard(name);
        verifyEndTurn();
        return usedToolCard;
    }

    private void verifyEndTurn(){
        if(diceExtracted && usedToolCard){
            cliHandler.notifyEndTurn();
            pass();
        }
    }

    void pass(){
        server.pass();
    }

    void addPlayers(List<String> newPlayers){
        cliHandler.printNewPlayers(newPlayers);
    }

    void removePlayer(String nickname){
        cliHandler.printLoggedOutPlayer(nickname);
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

    void printSchemas(List<Schema> schemas){
        Constraint constraint;
        Schema currentSchema;
        cliHandler.notifyStartGame();
        ClientLogger.println("Choose your schema:");
        for(int i=0;i<schemas.size();i+=2){
            ClientLogger.println("");
            ClientLogger.println("Schema ("+(i+1)+")               Schema ("+(i+2)+")");
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
        PlayerSnapshot p = gameSnapshot.getPlayer();
        List<PlayerSnapshot> otherPlayers = gameSnapshot.getOtherPlayers();
        int whiteSpaceNum,opNum=otherPlayers.size();
        Window currentWindow;
        Constraint constraint;

        ClientLogger.println("");
        ClientLogger.print(p.getNickname());
        whiteSpaceNum = 25 - p.getNickname().length();
        for(int i=0;i<whiteSpaceNum;i++) ClientLogger.print(" ");
        ClientLogger.print("|");
        for(PlayerSnapshot op : otherPlayers){
            ClientLogger.print("    "+op.getNickname());
            whiteSpaceNum = 21 - op.getNickname().length();
            for(int i=0;i<whiteSpaceNum;i++) ClientLogger.print(" ");
        }
        ClientLogger.println("");

        ClientLogger.print(CLI_21_DASH+"    |");
        for(int i=0;i<opNum;i++)
            ClientLogger.print("    "+CLI_21_DASH);
        ClientLogger.println("");

        for(int r=0;r<ROWS_NUMBER;r++){
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
                ClientLogger.print("    ");
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


            ClientLogger.print(CLI_21_DASH+"    |");
            for(int i=0;i<opNum;i++)
                ClientLogger.print("    "+CLI_21_DASH);
            ClientLogger.println("");
        }

        // print draftpool
        ClientLogger.println("");
        ClientLogger.println("Draft Pool                 |  Round Track");
        for(Dice dice : gameSnapshot.getDraftPool()){
            ClientLogger.print(dice+"  ");
        }
        for(int i=gameSnapshot.getDraftPool().size();i<9;i++) ClientLogger.print("   ");
        ClientLogger.print("|");
        for(Dice dice : gameSnapshot.getRoundTrack()){
            ClientLogger.print("  "+dice);
        }
        for(int i=gameSnapshot.getRoundTrack().size();i<9;i++) ClientLogger.print("  \u25A1");

        ClientLogger.println("");

        cliHandler.printMenu();
        if(!cliHandler.getPlayingRound()) {
            cliHandler.setPlayingRound(true);
            synchronized (cliHandler) {
                cliHandler.notifyAll();
            }
        }
    }

    private void initGameSnapshot(){
        this.gameSnapshot = new GameSnapshot(this.nickname);
    }

    GameSnapshot getGameSnapshot(){
        return gameSnapshot;

    }

    //region TOOLCARD

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

    //endregion

    public static void main(String[] args) {
        Client client = new Client();
        CLIHandler cliHandler;
        String mode = "CLI";
        Scanner in = new Scanner(System.in);
        boolean ok = false;
        while (!ok) {
            ClientLogger.print("Choose how you want to play:\n- CLI\n- GUI\nYour choice: ");
            mode = in.nextLine().toUpperCase();
            if (mode.equals("CLI") || mode.equals("GUI"))
                ok = true;
            else
                ClientLogger.println("Invalid choice!\n");
        }

        switch (mode){
            case "CLI":
                cliHandler = new CLIHandler(client);
                client.setCliHandler(cliHandler);
                cliHandler.start();
                break;
            case "GUI":
                break;
            default:
                break;
        }
        client.logout();
    }
}
