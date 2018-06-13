package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.model.Coordinate;
import it.polimi.ingsw.network.client.model.Dice;
import it.polimi.ingsw.network.client.model.PrivateGoal;
import it.polimi.ingsw.network.client.model.ToolCard;
import it.polimi.ingsw.network.client.model.exception.InvalidFavorTokenNumberException;
import it.polimi.ingsw.network.client.model.exception.NotEnoughFavorTokenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

class CLIHandler {

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
        String command = "";
        boolean ok = false;

        do {
            client.setServerAddress(askServerAddress());
            client.setServerPort(askServerPort());
        }while (!client.createConnection(askConnectionType()));

        waitConnection();

        while(!ok) {
            ClientLogger.print("Choose an option:\n- Logout\n- Login\nYour choice: ");
            try {
                command = input.readLine().toLowerCase();
            } catch (IOException e) {
                LOGGER.warning(e.toString());
            }
            if(command.equals("logout")) {
                ClientLogger.printlnWithClear("Logged out");
                return;
            }
            if(command.equals("login")) {
                if(client.login(askNickname(), askPassword())) {
                    client.setLogged(true);
                    ok = true;
                }
                else
                    ClientLogger.println("Login failed, password is not correct\n");
            }else
                ClientLogger.println("Invalid choice!\n");
        }
        waitStartRound();

        while (!logout){
            try {
                command = input.readLine().toLowerCase();
            } catch (IOException e) {
                LOGGER.severe(e.toString());
            }
            switch (command){
                case "logout":
                    ClientLogger.printlnWithClear("Logged out");
                    client.logout();
                    logout = true;
                    break;
                case "pass":
                    pass();
                    break;
                case "place dice":
                    if(!client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted())
                        placeDice();
                    else
                        ClientLogger.print("Invalid choice, dice already extracted\n\nRetry: ");
                    break;
                case "toolcard":
                    if(client.getGameSnapshot().getPlayer().getFavorToken() < 1)
                        ClientLogger.print("Not enough favor token!\n\nRetry: ");
                    else
                        useToolCard();
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

    void setPlayingRound(boolean playindRound){
        this.playingRound = playindRound;
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
            //ClientLogger.printlnWithClear("You passed your turn\n");
            client.pass();
        }else
            ClientLogger.println("\nNot your turn! You can only logout");
    }

    private void placeDice(){
        if(client.getGameSnapshot().getPlayer().isMyTurn()){
            int dice = -1, row = -1, column = -1;
            boolean ask = true;
            ClientLogger.printWithClear("");
            client.printGame();

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
                client.printGame();
                ClientLogger.println("\nConstraint violated!");
                client.printMenu();
            }
        }else
            ClientLogger.println("Not your turn! You can only logout");
    }

    private void useToolCard(){
        if(client.getGameSnapshot().getPlayer().isMyTurn()){
            int choice = -1;
            ClientLogger.printWithClear("");
            client.printGame();
            //printToolCards(client.getGameSnapshot().getToolCards());
            ClientLogger.println("\n[0] to go back");
            while (choice < 0 || choice > 3) {
                ClientLogger.print("\nInsert tool card number: ");
                try {
                    choice = Integer.parseInt(input.readLine());
                } catch (IOException | NumberFormatException e) {
                    choice = -1;
                }
                if(choice == 0) {
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
                client.printGame();
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
        }else
            ClientLogger.println("\nNot your turn! You can only logout");
    }

    void printToolCards(List<ToolCard> toolCards){
        int i = 0;
        ClientLogger.println("TOOL CARDS");
        for (ToolCard toolcard: toolCards) {
            ClientLogger.println(++i + ") " + toolcard.getName());
            //ClientLogger.println("   Description: " + toolcard.getDescription());
            ClientLogger.println("   Cost: " + (toolcard.getUsed() ? "2" : "1"));
        }
    }

    void printPublicGoals(List<String> publicGoals){

    }

    void printPrivateGoal(PrivateGoal privateGoal){

    }

    void clear(){
        ClientLogger.printWithClear("");
    }

    void printMenu(){
        if(client.getGameSnapshot().getPlayer().isMyTurn())
            ClientLogger.print("\nChoose an option:\n- Logout" + (!client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted() ? "\n- Place dice" : "") + (!client.getGameSnapshot().getPlayer().isToolCardAlreadyUsed() ? "\n- Toolcard" : "") + "\n- Pass\n\nYour choice: ");
        else
            ClientLogger.println("\nIf you want you can logout");
    }

    void notifyEndTurn(){
        ClientLogger.println("Your turn is finished!");
    }

    boolean getPlayingRound(){
        return playingRound;
    }

    void notifyServerDisconnected(){
        ClientLogger.printWithClear("");
        ClientLogger.println("Server disconnected");
    }

//    private int readInt(){
//        boolean ok = false;
//        int value = 0;
//        while (!ok){
//            try{
//                value = Integer.parseInt(input.readLine());
//                ok = true;
//            }
//            catch (IOException | NumberFormatException e){
//                ClientLogger.print("Must be a number, retry: ");
//            }
//        }
//        return value;
//    }

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

//            if (client.getGameSnapshot().getPlayer().getWindow().getCell(coordinate.getRow(), coordinate.getColumn()) != null)
//                ClientLogger.println("Invalid choice!");
//            else
                ask = false;
        }
        return coordinate;
    }

    int askDiceValue(){
        ClientLogger.print("Insert dice number: ");
        return readInt(1, 6);
    }

    private Coordinate getPosition(){
        int row, column;
        ClientLogger.print("Insert dice row: ");
        row = readInt(1, ROW);
        ClientLogger.print("Insert dice column: ");
        column = readInt(1, COLUMN);
        return new Coordinate(row, column);
    }

    //endregion
}
