package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.model.*;
import it.polimi.ingsw.network.client.model.exception.InvalidFavorTokenNumberException;
import it.polimi.ingsw.network.client.model.exception.NotEnoughFavorTokenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

class CLIHandler {

    private static final String CLI_SCHEMA_ROW = "---------------------    ---------------------";
    private static final String CLI_21_DASH = "---------------------";
    private static final int ROWS_NUMBER = 4;
    private static final int COLUMNS_NUMBER = 5;
    private static final int WINDOW_WIDTH = 27;

    private static final int ROW  = 4;
    private static final int COLUMN  = 5;
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName() );
    private BufferedReader input;
    private Client client;
    private boolean playingRound;

    CLIHandler(Client client){
        ClientLogger.initLogger(LOGGER);
        input = new BufferedReader(new InputStreamReader(System.in));
        this.client = client;
    }

    synchronized void start() {
        boolean logout = false;
        int command;
        boolean ok = false;

        do {
            client.setServerAddress(askServerAddress());
            client.setServerPort(askServerPort());
        }while (!client.createConnection(askConnectionType()));

        waitConnection();

        while(!ok) {
            ClientLogger.print("Choose an option:\n0) Logout\n1) Login\nYour choice: ");
                command = readInt(0, 1);

            if(command == 0) {
                ClientLogger.printlnWithClear("Logged out");
                return;
            }

            if(client.login(askNickname(), askPassword())) {
                client.setLogged(true);
                ok = true;
            }
            else
                ClientLogger.println("Login failed, password is not correct\n");
        }
        waitStartRound();

        while (!logout){

            command = readInt(0, 3);
            if(command > 0 && !client.getGameSnapshot().getPlayer().isMyTurn())
                ClientLogger.print("Invalid choice, retry: ");
            else
                switch (command){
                    case 0:
                        ClientLogger.printlnWithClear("Logged out");
                        client.logout();
                        logout = true;
                        break;
                    case 1:
                        if(!client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted())
                            placeDice();
                        else
                            useToolCard();
                        break;
                    case 2:
                        if(client.getGameSnapshot().getPlayer().isToolCardAlreadyUsed() || client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted())
                            pass();
                        else
                            useToolCard();
                        break;
                    case 3:
                        if(!client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted() && !client.getGameSnapshot().getPlayer().isToolCardAlreadyUsed())
                            pass();
                        else
                            ClientLogger.print("Invalid choice, retry: ");
                        break;
                    default:
                        ClientLogger.print("Invalid choice, retry: ");
            }
        }
    }

    private String askServerAddress(){

        String address = "";
        boolean ok = false;
        while (!ok) {
            ClientLogger.printWithClear("Insert server address: ");
            try {
                address = input.readLine();
            } catch (IOException e) {
                LOGGER.warning(e.toString());
                continue;
            }
            if (address.equals("") || address.contains(" "))
                ClientLogger.println("Invalid server address");
            else
                ok = true;
        }
        return  address;
    }

    private int askServerPort(){

        int port = 0;
        boolean ok = false;

        while (!ok) {
            ClientLogger.print("Insert server port: ");
            try {
                port = Integer.parseInt(input.readLine());
                if (port < 1000 || port > 65535)
                    ClientLogger.println("Invalid server port");
                else
                    ok = true;
            } catch (IOException | NumberFormatException e) {
                ClientLogger.println("Server port must be a number");
            }
        }
        return port;
    }

    private Client.ConnectionType askConnectionType(){
        int choice = -1;

        while (choice != 0 && choice != 1) {
            ClientLogger.println("\nConnection types:");
            ClientLogger.println("0) Socket");
            ClientLogger.println("1) RMI");
            ClientLogger.print("Your choice: ");

            try {
                choice = Integer.parseInt(input.readLine());
            } catch (IOException | NumberFormatException e) {
                continue;
            }
            if(choice != 0 && choice != 1)
                ClientLogger.println("Not a valid choice");
        }
        if(choice == 1)
            return Client.ConnectionType.RMI;
        return Client.ConnectionType.SOCKET;
    }

    private synchronized void waitConnection(){
        while (!client.getServerConnected())
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
    }

    void welcomePlayer(){
        ClientLogger.printlnWithClear("Welcome to Sagrada!\n");
    }

    private String askNickname(){
        String nickname = "user";
        boolean ok = false;

        while (!ok) {
            ClientLogger.printWithClear("Insert your nickname: ");
            try {
                nickname = input.readLine();
            } catch (IOException e) {
                LOGGER.severe(e.toString());
            }
            if (!checkNicknameProperties(nickname))
                    ClientLogger.println("Invalid nickname");
                else
                    ok = true;
        }
        return nickname;
    }

    private String askPassword(){
        String password = null;
        boolean ok = false;

        while (!ok) {
            ClientLogger.print("Insert your password: ");
            try {
                password = input.readLine();
            } catch (IOException e) {
                LOGGER.severe(e.toString());
            }

            if (!checkPasswordProperties(password))
                ClientLogger.println("Invalid password, must be at least 4 character");
            else
                ok = true;
        }
        return password;
    }

    private boolean checkNicknameProperties(String user){
        return user != null && !user.equals("");
    }

    private boolean checkPasswordProperties(String password){
        return password != null && !password.equals("") && password.length() >= 4;
    }

    void notifyStartGame(){
        ClientLogger.printlnWithClear("GAME STARTED!\n");
    }

    private synchronized void waitStartRound(){
        while(!playingRound)
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.severe(e.toString());
            }
    }

    void printWaitingRoom(){
        ClientLogger.printlnWithClear("Waiting for game to start!");
        ClientLogger.println("\nWaiting room:");
        ClientLogger.println(client.getGameSnapshot().getPlayer().getNickname());
        client.getGameSnapshot().getOtherPlayers().forEach(nick -> ClientLogger.println(nick.getNickname()));
    }

    //region DEPRECATED
    void printNewPlayers(List<String> newPlayers){
        if(!client.isLogged()){
            ClientLogger.printlnWithClear("Waiting for game to start!");
            ClientLogger.println("\nWaiting room:");
            ClientLogger.println(client.getGameSnapshot().getPlayer().getNickname());
            newPlayers.forEach(ClientLogger::println);
        }
        else
            newPlayers.forEach(ClientLogger::println);
    }

    void printLoggedOutPlayer(String nickname){
        ClientLogger.println(nickname + " logged out");
    }
    //endregion

    int chooseSchema(){
        int choice = 0;
        while (choice == 0) {
            ClientLogger.print("\nInsert your choice: ");

            try {
                choice = Integer.parseInt(input.readLine());
            }catch (IOException | NumberFormatException e){
                ClientLogger.println("Invalid choice");
                continue;
            }

            if(choice < 1 || choice > 4 || !client.sendSchema(choice - 1)){
                choice = 0;
                ClientLogger.println("Invalid choice");
            }
        }
        ClientLogger.println("\nWaiting other players' choice");
        return choice - 1;
    }

    void setPlayingRound(boolean playingRound){
        this.playingRound = playingRound;
    }

    void notifyNewTurn(String nickname, boolean newRound){
        if(newRound)
            ClientLogger.printlnWithClear("NEW ROUND STARTED!\n");
        ClientLogger.println("It's " + nickname  + "'s turn, wait for your turn\n");
    }

    void notifyNewTurn(boolean newRound){
        if(newRound)
            ClientLogger.printlnWithClear("NEW ROUND STARTED!\n");
        ClientLogger.println("It's your turn\n");

    }

    private void pass(){
        if(client.getGameSnapshot().getPlayer().isMyTurn()){
            client.pass();
        }else
            ClientLogger.println("\nNot your turn! You can only logout");
    }

    private void placeDice(){
        if(client.getGameSnapshot().getPlayer().isMyTurn()){
            int dice = -1, row = -1, column = -1;
            boolean ask = true;
            ClientLogger.printWithClear("");
            printGame(client.getGameSnapshot());

            while (ask) {
                ClientLogger.print("\nInsert dice number: ");
                dice = readInt(1, client.getGameSnapshot().getDraftPool().size());
                ClientLogger.print("Insert row: ");
                row = readInt(1, ROW);
                ClientLogger.print("Insert column: ");
                column = readInt(1, COLUMN);

                if (dice > client.getGameSnapshot().getDraftPool().size())
                    ClientLogger.println("Invalid choice!");
                else
                    ask = false;
            }
            if(!client.placeDice(dice, row, column)) {
                ClientLogger.printWithClear("");
                printGame(client.getGameSnapshot());
                ClientLogger.println("\nConstraint violated!");
                client.printMenu();
            }else
                verifyEndTurn();

        }else
            ClientLogger.println("Not your turn! You can only logout");
    }

    private void verifyEndTurn(){
        if(client.verifyEndTurn())
            client.pass();
        else {
            ClientLogger.printWithClear("");
            printGame(client.getGameSnapshot());
            client.printMenu();
        }
    }

    private void useToolCard(){
        if(client.getGameSnapshot().getPlayer().getFavorToken() < 1) {
            ClientLogger.print("Not enough favor token!\n\nRetry: ");
            return;
        }

        int choice = -1;
        ClientLogger.printWithClear("");
        printGame(client.getGameSnapshot());
        ClientLogger.println("\nTo go back insert 0");
        while (choice < 0 || choice > 3) {
            ClientLogger.print("\nInsert tool card number: ");
            try {
                choice = Integer.parseInt(input.readLine());
            } catch (IOException | NumberFormatException e) {
                choice = -1;
            }
            if(choice == 0) {
                ClientLogger.printWithClear("");
                printGame(client.getGameSnapshot());
                printMenu();
                return;
            }
            if(choice < 1 || choice > 3)
                ClientLogger.println("Not a valid choice");
        }
        ClientLogger.println("");
        if(!client.useToolCard( client.getGameSnapshot().getToolCards().get(choice - 1).getName())) {
            ClientLogger.printWithClear("");
            ClientLogger.println("You can't use this card now\n");
            printGame(client.getGameSnapshot());
            printMenu();
        }
        else {
            try {
                client.getGameSnapshot().getPlayer().useFavorToken((client.getGameSnapshot().getToolCards().get(choice - 1).getUsed() ? 2 : 1));
            } catch (InvalidFavorTokenNumberException | NotEnoughFavorTokenException e) {
            //TODO: check this
            }
            client.getGameSnapshot().getToolCards().get(choice - 1).setUsed();
        }

    }

    void printMenu(){
        if(client.getGameSnapshot().getPlayer().isMyTurn()) {
            ClientLogger.print("\nChoose an option:\n0) Logout");
            if (!client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted()) {
                ClientLogger.print("\n1) Place dice");
                if(!client.getGameSnapshot().getPlayer().isToolCardAlreadyUsed())
                    ClientLogger.print("\n2) Tool Card\n3) Pass\n\nYour choice: ");
                else
                    ClientLogger.print("\n2) Pass\n\nYour choice: ");
            }
            else
                ClientLogger.print("\n1) Tool Card\n2) Pass\n\nYour choice: ");
        }
        else
            ClientLogger.println("\nTo logout insert 0");
    }

    boolean getPlayingRound(){
        return playingRound;
    }

    void notifyServerDisconnected(){
        ClientLogger.printWithClear("");
        ClientLogger.println("Server disconnected");
    }

    private int readInt(int minValue, int maxValue){
        int value = -1;
        boolean ask = true;

        while (ask) {
            try{
                value = Integer.parseInt(input.readLine());
            }
            catch (IOException | NumberFormatException e){
                ClientLogger.print("Must be a number, retry: ");
                continue;
            }

            if (value < minValue || value > maxValue)
                ClientLogger.print("Invalid choice, retry: ");
            else
                ask = false;
        }
        return value;
    }

    //region TOOLCARD

    void notifyUsedToolCard(String player, String toolCard){
        ClientLogger.println(player + " used the tool card " + toolCard);
    }

    String askPlusMinusOption(){
        String choice = "";
        boolean ask = true;
        ClientLogger.print("Do you want to add [+] or subtract [-] 1? ");

        while (ask){
            try {
                choice = input.readLine();
            } catch (IOException e) {
                ClientLogger.print("Not a valid choice, retry: ");
            }

            if(choice.equals("+") || choice.equals("-"))
                ask = false;
            else
                ClientLogger.print("Not a valid choice, retry: ");
        }
        return choice;
    }

    Dice askDiceFormDraftPool(){
        ClientLogger.print("Insert dice number from draftpool: ");
        return client.getGameSnapshot().getDraftPool().get(readInt(1, client.getGameSnapshot().getDraftPool().size()) - 1);
    }

    int askDiceFormRoundTrack(){
        ClientLogger.print("Insert dice number from round track: ");
        return readInt(1, client.getGameSnapshot().getRoundTrack().size()) - 1;
    }

    Coordinate askDiceFormWindow(){
        Coordinate coordinate = null;
        boolean ask = true;

        ClientLogger.println("Choose a cell from your window");
        while (ask) {
            coordinate = getPosition();

//            if (client.getGameSnapshot().getPlayer().getWindow().getCell(coordinate.getRow(), coordinate.getColumn()) == null)
//                ClientLogger.println("Invalid choice!");
//            else
                ask = false;
        }
        return coordinate;
    }

    Coordinate askPlacementPosition(){
        Coordinate coordinate = null;
        boolean ask = true;

        ClientLogger.println("Choose a free position on your window");
        while (ask) {
            coordinate = getPosition();
                ask = false;
        }
        return coordinate;
    }

    int askDiceValue(){
        ClientLogger.print("Insert dice number: ");
        return readInt(1, 6);
    }

    private Coordinate getPosition(){
        ClientLogger.print("Insert dice row: ");
        int row = readInt(1, ROW);
        ClientLogger.print("Insert dice column: ");
        int column = readInt(1, COLUMN);
        return new Coordinate(row, column);
    }

    //endregion

    static void printFooter(GameSnapshot gameSnapshot){
        printPublicGoals(gameSnapshot.getPublicGoals());
        printPrivateGoal(gameSnapshot.getPlayer().getPrivateGoal());
        printToolCards(gameSnapshot.getToolCards());
    }

    static void printSchemas(List<Schema> schemas){
        ClientLogger.println("\nSCHEMA CHOICE");
        for(int i=0;i<schemas.size();i+=2){
            ClientLogger.println("");
            ClientLogger.print((i + 1) + ") " + schemas.get(i).getName());
            for(int s = schemas.get(i).getName().length(); s < 22; s++)
                ClientLogger.print(" ");
            ClientLogger.println((i + 2) + ") " + schemas.get(i + 1).getName());
            ClientLogger.println(CLI_SCHEMA_ROW);
            for(int r=0;r<ROWS_NUMBER;r++){
                printSchemaRow(new Window(schemas.get(i)), r);
                ClientLogger.print("|    ");
                printSchemaRow(new Window(schemas.get(i + 1)), r);
                ClientLogger.println("|");
                ClientLogger.println(CLI_SCHEMA_ROW);
            }
            ClientLogger.println("Difficulty: "+schemas.get(i).getDifficulty()+"            Difficulty: "+schemas.get(i+1).getDifficulty());
        }
    }

    static void printGame(GameSnapshot gameSnapshot){
        PlayerSnapshot p = gameSnapshot.getPlayer();
        List<PlayerSnapshot> otherPlayers = gameSnapshot.getOtherPlayers();
        int opNum=otherPlayers.size();
        Window currentWindow;

        printPlayers(gameSnapshot);

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
            printSchemaRow(currentWindow, r);
            ClientLogger.print("|    |");

            for(PlayerSnapshot op : otherPlayers){
                currentWindow = op.getWindow();
                ClientLogger.print("    " + (r + 1) + " ");
                printSchemaRow(currentWindow, r);
                ClientLogger.print("|");
            }
            ClientLogger.println("");


            ClientLogger.print("  " + CLI_21_DASH+"    |");
            for(int i=0;i<opNum;i++)
                ClientLogger.print("      "+CLI_21_DASH);
            ClientLogger.println("");
        }

        // print draft pool
        ClientLogger.println("");
        ClientLogger.println("  Draft Pool               |    Round Track");
        for(Dice dice : gameSnapshot.getDraftPool()){
            ClientLogger.print("  "+dice);
        }
        for(int i=gameSnapshot.getDraftPool().size();i<8;i++) ClientLogger.print("   ");
        ClientLogger.print("   |  ");
        for(Dice dice : gameSnapshot.getRoundTrack()){
            ClientLogger.print("  "+dice);
        }
        for(int i=gameSnapshot.getRoundTrack().size();i<9;i++) ClientLogger.print("  \u25A1");

        ClientLogger.println("\n");
        printFooter(gameSnapshot);
    }

    private static void printSchemaRow(Window currentWindow, int row) {
        Constraint constraint;
        for(int column = 0; column<COLUMNS_NUMBER; column++){
            if(currentWindow.getCell(row,column)!=null) ClientLogger.print("| "+currentWindow.getCell(row,column)+" ");
            else {
                constraint = currentWindow.getSchema().getConstraint(row,column);
                if(constraint==null) ClientLogger.print("|   ");
                else if(constraint.getColor()!=null) ClientLogger.print("| "+ constraint.getColor().escape() +"■ "+Color.RESET);
                else if(constraint.getNumber()!=0) ClientLogger.print("| "+constraint.getNumber()+" ");
            }
        }
    }

    private static void printPlayers(GameSnapshot gameSnapshot){
        PlayerSnapshot p = gameSnapshot.getPlayer();
        List<PlayerSnapshot> otherPlayers = gameSnapshot.getOtherPlayers();
        ClientLogger.println("");

        printPlayer(p);

        ClientLogger.print("|");
        for(PlayerSnapshot op : otherPlayers){
            printPlayer(op);
        }
        ClientLogger.println("");
        for (int i = 0;i<WINDOW_WIDTH;i++) ClientLogger.print(" ");
        ClientLogger.println("|");

    }

    private static void printPlayer(PlayerSnapshot p){
        int i;
        int whiteSpaceHalf;
        int whiteSpaceNum;

        whiteSpaceNum = WINDOW_WIDTH - p.getNickname().length() - 1 - p.getWindow().getSchema().getDifficulty();
        whiteSpaceHalf = whiteSpaceNum/2;

        for(i=0;i<whiteSpaceHalf;i++) ClientLogger.print(" ");

        ClientLogger.print(p.getNickname()+" ");
        for(i=0;i<p.getFavorToken();i++) ClientLogger.print("\u26AB");
        for(;i<p.getWindow().getSchema().getDifficulty();i++) ClientLogger.print("\u26AA");

        if(whiteSpaceNum%2 == 1) whiteSpaceHalf++;
        for(i=0;i<whiteSpaceHalf;i++) ClientLogger.print(" ");
    }

    private static void printPublicGoals(List<String> publicGoals){

    }

    private static void printPrivateGoal(PrivateGoal privateGoal){

    }

    static void printToolCards(List<ToolCard> toolCards){
        int i = 0;
        ClientLogger.println("TOOL CARDS");
        for (ToolCard toolcard: toolCards) {
            ClientLogger.print(++i + ") " + toolcard.getName());
            for(int j = toolcard.getName().length();j<31;j++) ClientLogger.print(" ");
            //ClientLogger.println("   Description: " + toolcard.getDescription());
            ClientLogger.println("|  Cost: " + (toolcard.getUsed() ? "2" : "1"));
        }
    }



    void clear(){
        ClientLogger.printWithClear("");
    }
}
