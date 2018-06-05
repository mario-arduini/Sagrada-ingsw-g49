package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.exceptions.InvalidFavorTokenNumberException;
import it.polimi.ingsw.model.exceptions.NotEnoughFavorTokenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

class CLIHandler {

    private static final Logger LOGGER = Logger.getLogger( Client.class.getName() );
    private BufferedReader input;
    private Client client;
    private boolean playindRound;

    CLIHandler(Client client){
        input = new BufferedReader(new InputStreamReader(System.in));
        this.client = client;
    }


    synchronized void start() {
        boolean logout = false;
        String command = "";
        boolean ok = false;

        client.setServerAddress(askServerAddress());
        client.setServerPort(askServerPort());
        client.createConnection(askConnectionType());
        waitConnection();

        while(!ok) {
            ClientLogger.print("Choose an option:\n- Logout\n- Login\nYour choice: ");
            try {
                command = input.readLine().toLowerCase();
            } catch (IOException e) {
            }
            if(command.equals("logout")) {
                ClientLogger.println("Logged out");
                return;
            }
            if(command.equals("login")) {
                if(client.login(askNickname(), askPassword())) {
                    ClientLogger.println("Login successful");
                    client.setLogged(true);
                    ok = true;
                }
                else
                    ClientLogger.println("Login failed, password is not correct");
            }else
                ClientLogger.println("Invalid choice!");
        }
        ClientLogger.println("Waiting for game to start!");
        waitStartRound();

        while (!logout){
            try {
                command = input.readLine().toLowerCase();
            } catch (IOException e) {
            }
            switch (command){
                case "logout":
                    ClientLogger.println("Logged out");
                    client.logout();
                    logout = true;
                    break;
                case "pass":
                    pass();
                    break;
                case "place dice":
                    if(!client.diceAlreadyExtracted())
                        placeDice();
                    else
                        ClientLogger.print("Invalid choice, dice already extracted\nRetry: ");
                    break;
                case "toolcard":
                    if(client.getGameSnapshot().getPlayer().getFavorToken() < 1)
                        ClientLogger.print("Not enough favor token!");
                    else
                        useToolCard();
                    break;
                default:
                    ClientLogger.print("Invalid choice, retry: ");
            }
        }
    }

    private String askServerAddress(){

        String address = "localhost";
        boolean ok = false;
        while (!ok) {
            ClientLogger.print("Insert server address: ");
            try {
                address = input.readLine();
            } catch (IOException e) {
            }
            if (address.equals("") || address.equals(" ") || address.contains(" "))
                ClientLogger.println("Invalid server address");
            else
                ok = true;
        }
        return  address;
    }

    private int askServerPort(){

        int port = 31337;
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
            ClientLogger.println("Connection types:");
            ClientLogger.println("[0] Socket");
            ClientLogger.println("[1] RMI");
            ClientLogger.print("Your choice: ");

            try {
                choice = Integer.parseInt(input.readLine());
            } catch (IOException | NumberFormatException e) {
                choice = -1;
            }
            if(choice != 0 && choice != 1)
                ClientLogger.println("Not a valid choice");
        }
        if(choice == 1){
            ClientLogger.println("You chose RMI");
            return Client.ConnectionType.RMI;
        }
        ClientLogger.println("You chose Socket");
        return Client.ConnectionType.SOCKET;
    }

    private synchronized void waitConnection(){
        while (!client.getServerConnected())
            try {
                wait();
            } catch (InterruptedException e) {
            }
    }

    void welcomePlayer(){
        ClientLogger.println("Welcome to Sagrada!");
    }

    private String askNickname(){
        String nickname = "user";
        boolean ok = false;

        while (!ok) {
            ClientLogger.print("Insert your nickname: ");
            try {
                nickname = input.readLine();
            } catch (IOException e) {
                e.printStackTrace();
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
            }

            if (!checkPasswordProperties(password))
                ClientLogger.println("Invalid password, must be at least 8 character");
            else
                ok = true;
        }
        return password;
    }

    private boolean checkNicknameProperties(String user){
        return user != null && !user.equals("");
    }

    private boolean checkPasswordProperties(String password){
        return password != null && !password.equals("") && password.length() >= 8;
    }

    void notifyStartGame(){
        ClientLogger.println("Game started!");
    }

    private synchronized void waitStartRound(){
        while(!playindRound)
            try {
                wait();
            } catch (InterruptedException e) {
            }
    }

    void printNewPlayers(List<String> newPlayers){
        if(!client.isLogged()){
            ClientLogger.println("Waiting room:");
            ClientLogger.println(client.getNickname());
            newPlayers.forEach(ClientLogger::println);
        }
        else
            newPlayers.forEach(name -> ClientLogger.println(name + " is now playing"));
    }

    void printLoggedOutPlayer(String nickname){
        ClientLogger.println(nickname + " logged out");
    }

    int chooseSchema(){
        int choice = 0;
        while (choice == 0) {
            ClientLogger.print("Insert your choice: ");

            try {
                choice = Integer.parseInt(input.readLine());
            }catch (IOException | NumberFormatException e){
                ClientLogger.println("Invalid choice");
            }

            if(choice < 1 || choice > 4 || !client.sendSchema(choice - 1)){
                choice = 0;
                ClientLogger.print("Invalid choice");
            }
        }
        ClientLogger.println("Waiting other players' choice");
        return choice - 1;
    }

    void setPlayingRound(boolean playindRound){
        this.playindRound = playindRound;
    }

    void notifyNewTurn(String nickname, boolean newRound){
        if(newRound)
            ClientLogger.println("New round started");
        ClientLogger.println("It's " + nickname  + "'s turn, wait for your turn");
    }

    void notifyNewTurn(boolean newRound){
        if(newRound)
            ClientLogger.println("New round started");
        ClientLogger.println("It's your turn");

    }

    private void pass(){
        if(client.isMyTurn()){
            ClientLogger.println("You passed your turn");
            client.pass();
        }else
            ClientLogger.println("Not your turn! You can only logout");
    }

    private void placeDice(){
        if(client.isMyTurn()){
            int dice, row, column;
            boolean ask = true;

            while (ask) {
                ClientLogger.print("Insert dice number: ");
                dice = readInt();
                ClientLogger.print("Insert row: ");
                row = readInt();
                ClientLogger.print("Insert column: ");
                column = readInt();

                if (dice > client.getGameSnapshot().getDraftPool().size() || dice <= 0 || !client.placeDice(dice, row, column))
                    ClientLogger.println("Invalid move!");
                else
                    ask = false;
            }
        }else
            ClientLogger.println("Not your turn! You can only logout");
    }

    private void useToolCard(){
        if(client.isMyTurn()){
            int choice = -1;
            printToolCards();
            while (choice < 0 || choice > 3) {
                ClientLogger.print("Your choice: ");
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
            if(!client.useToolCard( client.getGameSnapshot().getToolCards().get(choice - 1).getName())) {
                ClientLogger.println("You can't use this card now");
                printMenu();
            }
            else {
                try {
                    client.getGameSnapshot().getPlayer().useFavorToken((client.getGameSnapshot().getToolCards().get(choice - 1).getUsed() ? 2 : 1));
                } catch (InvalidFavorTokenNumberException | NotEnoughFavorTokenException e) {
                }
                client.getGameSnapshot().getToolCards().get(choice - 1).setUsed();
            }
        }else
            ClientLogger.println("Not your turn! You can only logout");
    }

    private void printToolCards(){
        int i = 0;
        ClientLogger.println("0) Go back");
        for (ToolCard toolcard: client.getGameSnapshot().getToolCards()) {
            ClientLogger.println(i + ") " + toolcard.getName());
            ClientLogger.println("   Description: " + toolcard.getDescription());
            ClientLogger.println("   Cost: " + (toolcard.getUsed() ? "2" : "1"));
        }
    }

    void printMenu(){
        if(client.isMyTurn())
            ClientLogger.print("Choose an option:\n- Logout" + (!client.diceAlreadyExtracted() ? "\n- Place dice" : "") + (!client.cardToolAlreadyUsed() ? "\n- Toolcard" : "") + "\n- Pass\nYour choice: ");
//            if(!client.diceAlreadyExtracted())
//                ClientLogger.print("Choose an option:\n- Logout\n- Place dice\n- Toolcard\n- Pass\nYour choice: ");
//            else
//                ClientLogger.print("Choose an option:\n- Logout\n- Toolcard\n- Pass\nYour choice: ");
        else
            ClientLogger.println("If you want you can logout");
    }

    void notifyEndTurn(){
        ClientLogger.println("Your turn is finished!");
    }

    boolean getPlayingRound(){
        return playindRound;
    }

    void notifyServerDisconnected(){
        ClientLogger.println("\nServer disconnected");
    }

    private int readInt(){
        boolean ok = false;
        int value = 0;
        while (!ok){
            try{
                value = Integer.parseInt(input.readLine());
                ok = true;
            }
            catch (IOException | NumberFormatException e){
                ClientLogger.print("Must be a number, retry: ");
            }
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
        return client.getGameSnapshot().getDraftPool().get(getIndexChoice(client.getGameSnapshot().getDraftPool().size()) - 1);
    }

    int askDiceFormRoundTrack(){
        ClientLogger.print("Insert dice number from roundtrack: ");
        return getIndexChoice(client.getGameSnapshot().getRoundTrack().size()) - 1;
    }

    private int getIndexChoice(int maxSize){
        int index = 0;
        boolean ask = true;

        while (ask) {
            index = readInt();

            if (index > maxSize || index <= 0)
                ClientLogger.print("Invalid choice, retry: ");
            else
                ask = false;
        }
        return index;
    }

    Coordinate askDiceFormWindow(){
        Coordinate coordinate = null;
        boolean ask = true;

        ClientLogger.print("Choose a dice from your window");
        while (ask) {
            coordinate = getPosition();

            if (client.getGameSnapshot().getPlayer().getWindow().getCell(coordinate.getRow(), coordinate.getColumn()) == null)
                ClientLogger.println("Invalid choice!");
            else
                ask = false;
        }
        return coordinate;
    }

    Coordinate askPlacementPosition(){
        Coordinate coordinate = null;
        boolean ask = true;

        ClientLogger.print("Choose a free position on your window");
        while (ask) {
            coordinate = getPosition();

            if (client.getGameSnapshot().getPlayer().getWindow().getCell(coordinate.getRow(), coordinate.getColumn()) != null)
                ClientLogger.println("Invalid choice!");
            else
                ask = false;
        }
        return coordinate;
    }

    private Coordinate getPosition(){
        int row, column;
        ClientLogger.print("Insert dice row: ");
        row = readInt();
        ClientLogger.print("Insert dice column: ");
        column = readInt();
        return new Coordinate(row, column);
    }

    //endregion
}
