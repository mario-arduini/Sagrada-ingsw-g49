package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.Color;

import java.util.ArrayList;
import java.util.List;
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
    private enum ConnectionType{ RMI, SOCKET }
    private ConnectionType connectionType;
    private List<String> players;
    private boolean logged;
    private boolean serverConnected;
    private GameSnapshot gameSnapshot;
    private boolean myTurn;
    private boolean waitingInput;
    private String inputConsole;
    private CLIListener cliListener;
    private Thread thread;

    private Client(){
        players = new ArrayList<>();
        cliListener = new CLIListener(this);
        thread = new Thread(cliListener);
        thread.start();
    }

    synchronized void welcomePlayer(){
        ClientLogger.println("Welcome to Sagrada!");
        serverConnected = true;
        notifyAll();
    }

    private synchronized void start(){
        while(serverAddress == null)
            serverAddress = askServerAddress();
        inputConsole = null;

        while(serverPort == 0)
            serverPort = askServerPort();
        inputConsole = null;

        while(connectionType == null)
            connectionType = askConnectionType();
        inputConsole = null;

        while(server == null)
            server = createConnection();

        while (!serverConnected) {
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, e.toString(), e);
            }
        }
        while(!isLogged())  //&& client.isServerConnected())
            if(!server.login())
                ClientLogger.println("Login failed, password is not correct");
            else{
                ClientLogger.println("Login successful");
                logged = true;
            }
        //logout();
    }

    private String askServerAddress(){

        //String address;
        ClientLogger.print("Insert server address: ");
        waitingInput = true;
        waitInput();
        //try {
            //address = input.readLine();
            if(inputConsole.equals("") || inputConsole.equals(" ") || inputConsole.contains(" ")) {
                ClientLogger.println("Invalid server address");
                inputConsole = null;
                return null;
            }
//        } catch (IOException e) {
//            LOGGER.log(Level.WARNING, e.toString(), e);
//            ClientLogger.println(INVALID_COMMAND);
//            return null;
//        }
        waitingInput = false;
        return  inputConsole;

//        String address;
//        ClientLogger.print("Insert server address: ");
//        try {
//            address = input.readLine();
//            if(address.equals("") || address.equals(" ") || address.contains(" ")) {
//                ClientLogger.println("Invalid server address");
//                return null;
//            }
//        } catch (IOException e) {
//            LOGGER.log(Level.WARNING, e.toString(), e);
//            ClientLogger.println(INVALID_COMMAND);
//            return null;
//        }
//        return  address;
    }

    private int askServerPort(){

        int port;
        ClientLogger.print("Insert server port: ");
        waitingInput = true;
        waitInput();

        try {
            port = Integer.parseInt(inputConsole);
        }catch (NumberFormatException e){
            ClientLogger.println("Server port must be a number");
            inputConsole = null;
            return 0;
        }

        if(port < 1000 || port > 65535) {
            ClientLogger.println("Invalid server port");
            inputConsole = null;
            return 0;
        }
        waitingInput = false;
        return  port;


//        int port;
//        ClientLogger.print("Insert server port: ");
//        try {
//            port = readInt();
//            if(port < 1000 || port > 65535) {
//                ClientLogger.println("Invalid server port");
//                return  0;
//            }
//        }
//        catch (NumberFormatException e){
//            ClientLogger.println("Server port must be a number");
//            return 0;
//        }
////        catch (IOException e) {
////            LOGGER.log(Level.WARNING, e.toString(), e);
////            ClientLogger.println(INVALID_COMMAND);
////            return 0;
////        }
//        return port;
    }

    private ConnectionType askConnectionType(){
        int choice;
        ClientLogger.println("Connection types:");
        ClientLogger.println("[0] Socket");
        ClientLogger.println("[1] RMI");
        ClientLogger.print("Your choice: ");
        waitingInput = true;
        waitInput();

        try {
            choice = Integer.parseInt(inputConsole);
        }catch (NumberFormatException e){
            ClientLogger.println("Not a valid choice");
            inputConsole = null;
            return null;
        }


        switch (choice) {
            case 0:
                ClientLogger.println("You chose Socket");
                waitingInput = false;
                return ConnectionType.SOCKET;
            case 1:
                ClientLogger.println("You chose RMI");
                waitingInput = false;
                return ConnectionType.RMI;
            default:
                ClientLogger.println("Not a valid choice");
                inputConsole = null;
                return null;
        }
    }

    private Connection createConnection(){
        if(connectionType == ConnectionType.SOCKET)
            return new ClientSocketHandler(this, serverAddress, serverPort);
        else if(connectionType == ConnectionType.RMI)
            return null;
        return null;
    }

    String askNickname(){
        while(inputConsole == null) {
            ClientLogger.print("Insert your nickname: ");
            waitingInput = true;
            waitInput();

            if(!checkNicknameProperties(inputConsole)) {
                inputConsole = null;
                ClientLogger.println("Invalid nickname");
            }
        }
        waitingInput = false;
        nickname = inputConsole;
        inputConsole = null;
        return nickname;
    }

    String askPassword(){
        String password;
        while(inputConsole == null) {
            ClientLogger.print("Insert your password: ");
            waitingInput = true;
            waitInput();

            if(!checkPasswordProperties(inputConsole)) {
                inputConsole = null;
                ClientLogger.println("Invalid password, must be at least 8 character");
            }
        }
        waitingInput = false;
        password = inputConsole;
        inputConsole = null;
        return password;
    }

    private boolean checkPasswordProperties(String password){
        return password != null && !password.equals("") && password.length() >= 8;
    }

    private boolean checkNicknameProperties(String user){
        return user != null && !user.equals("");
    }

    int chooseSchema(){
        int choice = 0;
        while (choice == 0) {
            ClientLogger.print("Insert your choice: ");
            waitingInput = true;
            waitInput();

            try {
                choice = Integer.parseInt(inputConsole);
            }catch (NumberFormatException e){
                ClientLogger.println("Invalid choice");
                inputConsole = null;
            }

            if(choice < 1 || choice > 4 || !server.sendSchema(choice - 1)){
                choice = 0;
                ClientLogger.print("Invalid choice");
                inputConsole = null;
            }
        }

        inputConsole = null;
        waitingInput = false;
        return choice - 1;
    }

    void notifyStartGame(){
        ClientLogger.println("Game started!");
    }

    void notifyNewRound(String nickname, boolean newRound){
        if(newRound)
            ClientLogger.println("New round started");

        if(nickname.equals(this.nickname)){
            ClientLogger.println("It's your turn");
            myTurn = true;
        }
        else{
            ClientLogger.println("It's " + nickname  + "'s turn");
            myTurn = false;
        }
    }

    void playRound(List<Dice> draftpool){
        int dice, row, column;
        boolean ask = true;
        if(myTurn)
        {
            while (ask) {
                ClientLogger.print("Insert dice number: ");
                dice = readInt();
                ClientLogger.print("Insert row: ");
                row = readInt();
                ClientLogger.print("Insert column: ");
                column = readInt();

                if (dice > draftpool.size() || dice <= 0 || !server.placeDice(draftpool.get(dice - 1), row, column))
                    ClientLogger.println("Invalid move!");
                else {

                    gameSnapshot.getPlayer().getWindow().addDice(row - 1, column - 1, draftpool.get(dice - 1));
                    gameSnapshot.getDraftPool().remove(dice - 1);
                    printGame();
                    server.pass();

                    ask = false;
                }
            }
        }
     }

     private int readInt(){
        boolean ok = false;
        int value = 0;
        while (!ok){
            try{
                waitingInput = true;
                waitInput();
                value = Integer.parseInt(inputConsole);
                ok = true;
            }
            catch (NumberFormatException e){
                ClientLogger.print("Must be a number, retry: ");
                inputConsole = null;
            }
        }
        inputConsole = null;
        waitingInput = false;
        return value;
     }

    void setPrivateGoal(String[] privateGoal){}

    void logout(){
//        try {
//            String command = input.readLine();
//            if(command.equals("logout")){
        if(serverConnected)
            server.logout();

        cliListener.setRead(false);
        thread.interrupt();

        ClientLogger.println("Logged out");
        logged = false;
        Thread.currentThread().interrupt();
//            }
//        } catch (IOException e) {
//            LOGGER.log(Level.WARNING, e.toString(), e);
//        }
    }

    private boolean isLogged(){
        return logged;
    }

    boolean getWaitingInput(){
        return waitingInput;
    }

    synchronized void setInput(String input){
        this.inputConsole = input;
        notifyAll();
    }

    private synchronized void waitInput(){
        while (inputConsole == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                //LOGGER.log(Level.WARNING, e.toString(), e);   //TODO exception when logout
            }
        }
        waitingInput = false;
    }

    void addPlayers(List<String> newPlayers){

        players.addAll(newPlayers);
        if(!logged){
            ClientLogger.println("Waiting room:");
            ClientLogger.println(nickname);
            newPlayers.forEach(ClientLogger::println);
        }
        else
            newPlayers.forEach(name -> ClientLogger.println(name + " is now playing"));
    }

    void removePlayer(String nickname){
        players.remove(nickname);
        ClientLogger.println(nickname + " logged out");
    }

    void serverDisconnected(){
        if(logged) {
            ClientLogger.println("\nServer disconnected");
            logged = false;
            serverConnected = false;
        }
    }

    void printSchemas(List<Schema> schemas){
        Constraint constraint;
        Schema currentSchema;
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
        ClientLogger.println("Draft Pool");
        for(Dice dice : gameSnapshot.getDraftPool()){
            ClientLogger.print(dice+"  ");
        }
        ClientLogger.println("");
    }

    void initGameSnapshot(){
        this.gameSnapshot = new GameSnapshot(this.nickname);
    }

    GameSnapshot getGameSnapshot(){
        return gameSnapshot;

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
