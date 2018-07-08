package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.client.model.*;
import it.polimi.ingsw.network.client.model.Color;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

/**
 * Handles all the interaction between the user and the CLI
 * Contains methods to print and get information from the CLI
 */
class CLIHandler implements GraphicInterface{

    private static final int MIN_PORT = 10000;
    private static final int MAX_PORT = 65535;
    private static final int WINDOW_WIDTH = 27;
    private static final int ROWS = 4;
    private static final int COLUMNS = 5;
    private static final String ERROR = "Invalid choice, retry: ";
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private Client client;
    private boolean waiting;
    private boolean serverResult;
    private boolean flagContinue;
    private boolean newGame;

    private boolean waitingInput;
    private boolean flagContinueInput;
    private String toolCardNotCompleted;
    private String inputResult;
    private Thread thread;
    private CLIListener cliListener;
    private final Object lock1;
    private boolean waitLock2;
    private final Object lock2;
    private final Object lockInput;

    /**
     * Creats a new objiect that handles the interaction between the user and the CLI
     * Start a new thread that always listens what the user writes on the CLI
     */
    CLIHandler() {
        ClientLogger.initLogger(LOGGER);
        flagContinue = false;
        waiting = false;
        newGame = true;
        waitingInput = false;
        flagContinueInput = false;
        toolCardNotCompleted = "";
        lock1 = new Object();
        lock2 = new Object();
        waitLock2 = false;
        lockInput = new Object();

        try {
            this.client = new Client(this);
        }catch (RemoteException e){
            LOGGER.warning(e.toString());
        }

        cliListener = new CLIListener(this);
        thread = new Thread(cliListener);
        thread.start();
    }

    /**
     * Aks some technical information to the user to connect to the server and to log in into the game
     * While the user wants to play it creates new games
     */
    synchronized void start() {
        int command;
        boolean ok = false;

        Client.ConnectionType connectionType;
        do {
            connectionType = askConnectionType();
            client.setServerAddress(askServerAddress());
            if(connectionType == Client.ConnectionType.SOCKET)
                client.setServerPort(askServerPort());
            flagContinue = false;
        }while (!client.createConnection(connectionType));

        waitLock2 = true;
        waitResult(lock2);

        while(!ok) {
            ClientLogger.printWithClear("Welcome to Sagrada!\n\nChoose an option:\n0) Logout\n1) Login\nYour choice: ");
                command = readInt(0, 1);

            if(command == 0) {
                ClientLogger.printlnWithClear("Logged out");
                cliListener.stopListening();
                thread.interrupt();
                return;
            }

            flagContinue = false;

            try {
                client.login(askNickname(), askPassword());
            } catch (ServerReconnectedException e) {
                LOGGER.info(e.toString());
            }

            waitLock2 = true;
            if(waitResult(lock2))
                ok = true;
            else
                ClientLogger.println("Login failed, password is not correct\n");
        }

        while (newGame) {
            flagContinue = false;
            try {
                if(!play()) {
                    if (client.getServerConnected())
                        newGame = askNewGame();
                }
                else
                    newGame = false;
            } catch (ServerReconnectedException e) {
                //waitResult(lock2);
                LOGGER.info(e.toString());
            }
        }
        cliListener.stopListening();
        thread.interrupt();
    }

    /**
     * Handles the flow of a single game of the user
     * @return true if the users wants to play a new game, false otherwise
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    private boolean play() throws ServerReconnectedException{
        boolean logout = false;
        int command;
        if (!client.isGameStarted()) {
            //waitLock2 = true;
            //waitResult(lock2);
            do {
                ClientLogger.print("\nYour choice: ");
                command = readInt(1, 4);
                if(command == -1)
                    return false;

                flagContinue = false;
                client.sendSchemaChoice(command - 1);
                if (!flagContinue)
                    ClientLogger.print("\nWaiting other players' choice");

                waitLock2 = true;
                waitResult(lock2);
            } while (!serverResult);
        }
        if(!client.isGameStarted())
            return false;

        if(!toolCardNotCompleted.equals("")){
            completeToolCard();
            toolCardNotCompleted = "";
        }

        while (!logout) {
            try {
                command = readInt(0, 3);
                if (!client.isGameStarted())
                    throw new InputInterruptedException();

                if (command > 0 && !client.getGameSnapshot().getPlayer().isMyTurn())
                    ClientLogger.print(ERROR);
                else
                    switch (command) {
                        case 0:
                            ClientLogger.printlnWithClear("Logged out");
                            cliListener.stopListening();
                            thread.interrupt();
                            client.logout();
                            logout = true;
                            break;
                        case 1:
                            if (!client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted())
                                placeDice();
                            else
                                useToolCard();
                            break;
                        case 2:
                            if (client.getGameSnapshot().getPlayer().isToolCardAlreadyUsed() || client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted())
                                client.pass();
                            else
                                useToolCard();
                            break;
                        case 3:
                            if (!client.getGameSnapshot().getPlayer().isDiceAlreadyExtracted() && !client.getGameSnapshot().getPlayer().isToolCardAlreadyUsed())
                                client.pass();
                            else
                                ClientLogger.print(ERROR);
                            break;
                        default:
                            break;
                    }
            }catch (InputInterruptedException e){
                break;
            }
        }
        return logout;
    }

    /**
     * Lets the users complete the use of a tool card already begun before a disconnection
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     */
    private void completeToolCard() throws ServerReconnectedException{
        ClientLogger.println("\nUsing the tool card " + toolCardNotCompleted + "\n");
        client.continueToolCard();
        waitResult(lock1);
    }

    /**
     * Asks the user if he wants to play a new game or log out
     * @return true if the users wants to play a new game, false otherwise
     */
    private boolean askNewGame(){
        boolean ok = true;
        ClientLogger.print("\nChoose an option:\n0) Logout\n1) New game\nYour choice: ");
        if(readInt(0, 1) == 1){
            flagContinue = false;
            while(ok) {
                try {
                    client.newGame();
                    ok = false;
                } catch (ServerReconnectedException e) {
                    LOGGER.info(e.toString());
                }
            }
            waitLock2 = true;
            waitResult(lock2);
            return true;
        }
        return false;
    }

    /**
     * Asks the users to insert the IP address of the server
     * @return the IP address of the server
     */
    private String askServerAddress(){

        String address = "";
        boolean ok = false;
        while (!ok) {
            ClientLogger.print("\nInsert server address: ");
            address = waitInput();
            if (address.equals("") || address.contains(" "))
                ClientLogger.println("Invalid server address");
            else
                ok = true;
        }
        return  address;
    }

    /**
     * Asks the users to insert the port of the server
     * @return the port of the server
     */
    private int askServerPort(){
        ClientLogger.print("Insert server port: ");
        return readInt(MIN_PORT, MAX_PORT);
    }

    /**
     * Asks the users to insert the type of connection (RMI or socket) to use with the server
     * @return the type of connection to use with the server the server
     */
    private Client.ConnectionType askConnectionType(){
        ClientLogger.printlnWithClear("Connection types:");
        ClientLogger.println("0) Socket");
        ClientLogger.println("1) RMI");
        ClientLogger.print("Your choice: ");

        if(readInt(0, 1) == 1)
            return Client.ConnectionType.RMI;
        return Client.ConnectionType.SOCKET;
    }

    /**
     * Asks which nickname to use to the user
     * @return the nickname chosen by the user
     */
    private String askNickname(){
        String nickname = "user";
        boolean ok = false;

        while (!ok) {
            ClientLogger.printWithClear("Insert your nickname: ");
            nickname = waitInput();
            if (!checkNicknameProperties(nickname))
                    ClientLogger.println("Invalid nickname");
                else
                    ok = true;
        }
        return nickname;
    }

    /**
     * Asks which password to use to the user
     * @return the password chosen by the user
     */
    private String askPassword(){
        String password = null;
        boolean ok = false;

        while (!ok) {
            ClientLogger.print("Insert your password: ");
            password = waitInput();

            if (!checkPasswordProperties(password))
                ClientLogger.println("Invalid password, must be at least 4 character");
            else
                ok = true;
        }
        return password;
    }

    /**
     * Checks if the nickname chosen by the user meets some requirements
     * @param user the nickname to check
     * @return true if the nickname meets the requirements, false otherwise
     */
    private boolean checkNicknameProperties(String user){
        return user != null && !user.equals("");
    }

    /**
     * Checks if the password chosen by the user meets some requirements
     * @param password the password to check
     * @return true if the password meets the requirements, false otherwise
     */
    private boolean checkPasswordProperties(String password){
        return password != null && !password.equals("") && password.length() >= 4;
    }

    /**
     * Prints on the CLI the nicknames of the users currently waiting for a game to start
     */
    @Override
    public void printWaitingRoom(){
        ClientLogger.printlnWithClear("Waiting for game to start");
        ClientLogger.println("\nWaiting room:");
        ClientLogger.println(client.getGameSnapshot().getPlayer().getNickname());
        client.getGameSnapshot().getOtherPlayers().forEach(nick -> ClientLogger.println(nick.getNickname()));
    }

    /**
     * Prints the public goals, the tool cards, the private goal
     * Prints the schemas the user can chose from
     * @param gameSnapshot
     * @param schemas the schemas the user can chose from
     */
    @Override
    public void printSchemaChoice(GameSnapshot gameSnapshot, List<Schema> schemas){
        ClientLogger.printlnWithClear("GAME STARTED!\n");
        printFooter(gameSnapshot);
        printSchemas(schemas);
    }


    /**
     * Asks which dice to place and a cell where to place it to the user
     * Asks the client to execute the move and waits for the result from the server, notifying the user
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     * @throws InputInterruptedException if the waiting for an input from the CLI was interrupted
     */
    private void placeDice() throws ServerReconnectedException, InputInterruptedException{
        if(client.getGameSnapshot().getPlayer().isMyTurn()){
            int dice = -1;
            int row = -1;
            int column = -1;
            boolean ask = true;
            printGame(client.getGameSnapshot());

            while (ask) {
                try {
                    ClientLogger.print("\nInsert dice number: ");
                    dice = readInt(1, client.getGameSnapshot().getDraftPool().size());
                    if(dice == -1)  throw new InputInterruptedException();
                    ClientLogger.print("Insert row: ");
                    row = readInt(1, ROWS);
                    if(row == -1)  throw new InputInterruptedException();
                    ClientLogger.print("Insert column: ");
                    column = readInt(1, COLUMNS);
                    if(column == -1)  throw new InputInterruptedException();
                }catch (CancellationException e){
                    return;
                }

                if (dice > client.getGameSnapshot().getDraftPool().size())
                    ClientLogger.println("Invalid choice!");
                else
                    ask = false;
            }
            flagContinue = false;
            client.placeDice(dice, row, column);
            waitLock2 = true;
            if(!waitResult(lock2)) {
                printGame(client.getGameSnapshot());
                ClientLogger.println("\nConstraint violated!");
                printMenu(client.getGameSnapshot());
            } else
                client.verifyEndTurn();
        }else
            ClientLogger.println("Not your turn! You can only logout");
    }

    /**
     * Asks which tool card to use to the user
     * Asks the client to execute the move and waits for the result from the server, notifying the user
     * @throws ServerReconnectedException if the connection with the server had gone down and a reconnection was successful
     * @throws InputInterruptedException if the waiting for an input from the CLI was interrupted
     */
    private void useToolCard() throws ServerReconnectedException, InputInterruptedException{
        if (client.getGameSnapshot().getPlayer().getFavorToken() < 1) {
            ClientLogger.print("Not enough favor token!\n\nRetry: ");
            return;
        }

        int choice = -1;
        printGame(client.getGameSnapshot());
        ClientLogger.println("\nTo go back insert 0");
        while (choice < 0 || choice > 3) {
            ClientLogger.print("\nInsert tool card number: ");
            try {
                choice = readInt(0, 3);
                if(choice == -1)  throw new InputInterruptedException();
            } catch (NumberFormatException e) {
                choice = -1;
            } catch (CancellationException e) {
                return;
            }

            if (choice == 0) {
                printGame(client.getGameSnapshot());
                printMenu(client.getGameSnapshot());
                return;
            }
            if (choice < 1 || choice > 3)
                ClientLogger.println("Not a valid choice");
        }

        ClientLogger.println("Insert 0 to go back to the menu.\n");

        flagContinue = false;
        client.useToolCard(client.getGameSnapshot().getToolCards().get(choice - 1).getName());
        if (!waitResult(lock1)) {
            if(!client.getServerConnected() && waitResult(lock1))
                throw new ServerReconnectedException();
            printGame(client.getGameSnapshot());
            ClientLogger.println("\nYou can't use this card now");
            printMenu(client.getGameSnapshot());
        } else {
            client.verifyEndTurn();
            if(client.getGameSnapshot().getPlayer().isMyTurn() && !client.getGameSnapshot().getPlayer().isToolCardAlreadyUsed()) {
                printGame(client.getGameSnapshot());
                printMenu(client.getGameSnapshot());
            }
        }
    }

    /**
     * Waits that the server respond to an action of the user
     * @param lock the object to lock on
     * @return true if the action of the user was successful, false otherwise
     */
    private boolean waitResult(Object lock){
        while (!flagContinue) {
            waiting = true;
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }
        waiting = false;
        flagContinue = false;
        return serverResult;
    }

    /**
     * Waits that the user inserts something on the CLI
     * @return what the user has written on the CLI
     */
    private String waitInput(){
        while (!flagContinueInput) {
            waitingInput = true;
            try {
                synchronized (lockInput) {
                    lockInput.wait();
                }
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }
        waitingInput = false;
        flagContinueInput = false;
        return inputResult;
    }

    /**
     * Reads an integer between a minimum and a maximum number from the CLI
     * @param minValue the minimum value accepted
     * @param maxValue the maximum value accepted
     * @return the value that the user inserted
     */
    private int readInt(int minValue, int maxValue){
        int value = -1;
        boolean ask = true;
        String choice;

        while (ask) {
            try{
                choice = waitInput();
                if(choice == null)
                    return -1;
                value = Integer.parseInt(choice);
            } catch (NumberFormatException e){
                ClientLogger.print("Must be a number, retry: ");
                continue;
            }

            if (value < minValue || value > maxValue)
                ClientLogger.print(ERROR);
            else
                ask = false;
        }
        return value;
    }

    /**
     * Prints a menu with same choice for the user based on the current situation of the turn
     * @param gameSnapshot
     */
    @Override
    public void printMenu(GameSnapshot gameSnapshot){
        if(gameSnapshot.getPlayer().isMyTurn()) {
            ClientLogger.print("\nIt's your turn\n\nChoose an option:\n0) Logout");
            if (!gameSnapshot.getPlayer().isDiceAlreadyExtracted()) {
                ClientLogger.print("\n1) Place dice");
                if(!gameSnapshot.getPlayer().isToolCardAlreadyUsed())
                    ClientLogger.print("\n2) Tool Card\n3) Pass\n\nYour choice: ");
                else
                    ClientLogger.print("\n2) Pass\n\nYour choice: ");
            }
            else
                ClientLogger.print("\n1) Tool Card\n2) Pass\n\nYour choice: ");
        }
        else
            ClientLogger.println("\nIt's " + gameSnapshot.getCurrentPlayer() + "'s turn, wait for your turn\n\nTo logout insert 0");
    }


    /**
     * Notifies the user that the connection with the server went down
     */
    @Override
    public void notifyServerDisconnected(){
        ClientLogger.printlnWithClear("Server disconnected, trying to reconnect");
    }

    //region TOOLCARD

    /**
     * Notifies that a user used a certain tool card
     * @param player the user who used the tool card
     * @param toolCard the tool card used
     */
    @Override
    public void notifyUsedToolCard(String player, String toolCard){
        ClientLogger.println("\n" + player + " used the tool card " + toolCard);
    }

    /** Asks the user if to increment or decrement the value of the dice
     * @param prompt the code of the message to print to the user
     * @param rollback true if the use can decide not to use the tool card anymore, false otherwise
     * @return true if user wants to the increment the value of the dice, false otherwise
     * @throws RollbackException if the users doesn't want to use the tool card anymore
     */
    @Override
    public boolean askIfPlus(String prompt, boolean rollback) throws RollbackException{
        String choice = "";
        boolean ask = true;
        ClientLogger.print(MessageHandler.get(prompt));

        while (ask){
            choice = waitInput();
            if (choice.equals("0") && rollback) throw new RollbackException();
            if(choice.equals("+") || choice.equals("-") || choice.equals("y") || choice.equals("n"))
                ask = false;
            else
                ClientLogger.print("Not a valid choice, retry: ");
        }
        return choice.equalsIgnoreCase("+");
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
        int startingValue;
        if (rollback)
            startingValue = 0;
        else
            startingValue = 1;

        ClientLogger.print(MessageHandler.get(prompt));
        int i = readInt(startingValue, client.getGameSnapshot().getDraftPool().size());
        if (i == 0)
            throw new RollbackException();
        return client.getGameSnapshot().getDraftPool().get(i - 1);
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
        int startingValue;
        if (rollback)
            startingValue = 0;
        else
            startingValue = 1;

        ClientLogger.print(MessageHandler.get(prompt));
        int i = readInt(startingValue, 10);
        if (i == 0){
            throw new RollbackException();
        }
        return i - 1;
    }

    /**
     * Asks the user to choose a free cell or a occupied one from the window based con the code message
     * @param prompt the code of the message to print to the user
     * @param rollback true if the use can decide not to use the tool card anymore, false otherwise
     * @return the cell form the windows chosen by the user
     * @throws RollbackException if the users doesn't want to use the tool card anymore
     */
    @Override
    public Coordinate askDiceWindow(String prompt, boolean rollback) throws RollbackException {
        ClientLogger.println(MessageHandler.get(prompt));
        return getPosition(rollback);
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
        int startingValue;
        if (rollback)
            startingValue = 0;
        else
            startingValue = 1;

        ClientLogger.print(MessageHandler.get(prompt));
        int i = readInt(startingValue, 6);
        if (i==0)
            throw new RollbackException();
        return i;
    }

    /**
     * Notifies the user that a dice has been inserted into the draft pool
     * @param dice the dice inserted into the draft pool
     */
    @Override
    public void alertDiceInDraftPool(Dice dice){
        ClientLogger.println(MessageHandler.get("alert-dice"));
        printDice(dice);
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
        int startingValue;
        if (rollback)
            startingValue = 0;
        else
            startingValue = 1;

        ClientLogger.print(MessageHandler.get(prompt) + n + ": ");
        int i = readInt(startingValue, n);
        if (i==0)
            throw new RollbackException();
        return i;
    }

    /**
     * Asks the user to choose a row and a column of a cell on the window
     * @param rollback true if the use can decide not to use the tool card anymore, false otherwise
     * @return the coordinate of the cell the user chose
     * @throws RollbackException if the users doesn't want to use the tool card anymore
     */
    private Coordinate getPosition(boolean rollback) throws RollbackException{
        int startingValue;
        if (rollback)
            startingValue = 0;
        else
            startingValue = 1;

        ClientLogger.print("Insert dice row: ");
        int row = readInt(startingValue, ROWS);
        if (row==0)
            throw new RollbackException();
        ClientLogger.print("Insert dice column: ");
        int column = readInt(startingValue, COLUMNS);
        if (column==0)
            throw new RollbackException();
        return new Coordinate(row, column);
    }

    /**
     * Prints to the users a specif dice
     * @param dice the dice to print
     */
    @Override
    public void printDice(Dice dice){
        ClientLogger.println("Dice: " + dice);
    }

    //endregion

    /**
     * Sets the name of the tool card that the use had already begun before a disconnection
     * @param toolCard the name of the tool card
     */
    public void setToolCardNotCompleted(String toolCard){
        this.toolCardNotCompleted = toolCard;
    }

    /**
     * Prints the public goals, the tool cards and the private goal on the CLI
     * @param gameSnapshot
     */
    private static void printFooter(GameSnapshot gameSnapshot){
        printPublicGoals(gameSnapshot.getPublicGoals());
        printPrivateGoal(gameSnapshot.getPlayer().getPrivateGoal());
        printToolCards(gameSnapshot.getToolCards());
    }

    /**
     * Prints on the CLI the schemas that the user can choose from to play
     * @param schemas the schemas the user can choose from
     */
    private static void printSchemas(List<Schema> schemas){
        final String CLI_SCHEMA_ROW = "---------------------    ---------------------";
        ClientLogger.println("\nSCHEMA CHOICE");
        for(int i=0;i<schemas.size();i+=2){
            ClientLogger.println("");
            ClientLogger.print((i + 1) + ") " + schemas.get(i).getName());
            for(int s = schemas.get(i).getName().length(); s < 22; s++)
                ClientLogger.print(" ");
            ClientLogger.println((i + 2) + ") " + schemas.get(i + 1).getName());
            ClientLogger.println(CLI_SCHEMA_ROW);
            for(int r = 0; r< ROWS; r++){
                printSchemaRow(new Window(schemas.get(i)), r);
                ClientLogger.print("|    ");
                printSchemaRow(new Window(schemas.get(i + 1)), r);
                ClientLogger.println("|");
                ClientLogger.println(CLI_SCHEMA_ROW);
            }
            ClientLogger.println("Difficulty: "+schemas.get(i).getDifficulty()+"            Difficulty: "+schemas.get(i+1).getDifficulty());
        }
    }

    /**
     * Prints all the information a the game that the user is currently playing, with the information about the other users
     * @param gameSnapshot
     */
    @Override
    public void printGame(GameSnapshot gameSnapshot){
        final String CLI_21_DASH = "---------------------";
        PlayerSnapshot p = gameSnapshot.getPlayer();
        List<PlayerSnapshot> otherPlayers = gameSnapshot.getOtherPlayers();
        int opNum=otherPlayers.size();
        Window currentWindow;

        ClientLogger.printWithClear("");
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

        for(int r = 0; r< ROWS; r++){
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
        for(int i=gameSnapshot.getRoundTrack().size();i<10;i++) ClientLogger.print("  \u25A1");

        ClientLogger.println("\n");
        printFooter(gameSnapshot);
    }

    /**
     * Prints a single row of a user's window on the CLI
     * @param currentWindow the windows whose row is printed
     * @param row the index of the row to print
     */
    private static void printSchemaRow(Window currentWindow, int row) {
        Constraint constraint;
        for(int column = 0; column< COLUMNS; column++){
            if(currentWindow.getCell(row,column)!=null) ClientLogger.print("| "+currentWindow.getCell(row,column)+" ");
            else {
                constraint = currentWindow.getSchema().getConstraint(row,column);
                if(constraint==null) ClientLogger.print("|   ");
                else if(constraint.getColor()!=null) ClientLogger.print("| "+ constraint.getColor().escape() +"■ "+Color.RESET);
                else if(constraint.getNumber()!=0) ClientLogger.print("| "+constraint.getNumber()+" ");
            }
        }
    }

    /**
     * Prints all the users that are playing the game
     * @param gameSnapshot
     */
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

    /**
     * Prints the information of a user playing the game
     * @param playerSnapshot the player whose information are printed
     */
    private static void printPlayer(PlayerSnapshot playerSnapshot){
        int i;
        int whiteSpaceHalf;
        int whiteSpaceNum;

        whiteSpaceNum = WINDOW_WIDTH - playerSnapshot.getNickname().length() - (playerSnapshot.isSuspended() ? 4 : 0) - 1 - playerSnapshot.getWindow().getSchema().getDifficulty();
        whiteSpaceHalf = whiteSpaceNum/2;

        for(i=0;i<whiteSpaceHalf;i++) ClientLogger.print(" ");

        ClientLogger.print(playerSnapshot.getNickname()+ (playerSnapshot.isSuspended() ? " (S) " : " "));
        for(i=0;i<playerSnapshot.getFavorToken();i++) ClientLogger.print("\u25CF");
        for(;i<playerSnapshot.getWindow().getSchema().getDifficulty();i++) ClientLogger.print("\u25CB");

        if(whiteSpaceNum%2 == 1) whiteSpaceHalf++;
        for(i=0;i<whiteSpaceHalf;i++) ClientLogger.print(" ");
    }

    /**
     * Prints the public goals of the current game
     * @param publicGoals the list of the names of the public goals to print
     */
    private static void printPublicGoals(List<String> publicGoals){
        StringBuilder names = new StringBuilder();
        for(String name : publicGoals)
            names.append(" ").append(name).append(",");
        names.deleteCharAt(names.length() - 1);
        ClientLogger.println("PUBLIC GOAL:" + names + "\n");
    }

    /**
     * Prints the private goal of the user
     * @param privateGoal the private goal to print
     */
    private static void printPrivateGoal(String privateGoal){
        ClientLogger.println("PRIVATE GOAL: " + privateGoal);
    }

    /**
     * Prints the tool cards of the current game
     * @param toolCards the list of the names of the tool cards to print
     */
    private static void printToolCards(List<ToolCard> toolCards){
        int i = 0;
        ClientLogger.println("\nTOOL CARDS");
        for (ToolCard toolcard: toolCards) {
            ClientLogger.print(++i + ") " + toolcard.getName());
            for(int j = toolcard.getName().length();j<31;j++) ClientLogger.print(" ");
            ClientLogger.println("|  Cost: " + (toolcard.getUsed() ? "2" : "1"));
        }
    }

    /**
     * Interrupts the wait of a read on the CLI
     */
    @Override
    public void interruptInput(){
        wakeUpInput(null);
    }

    /**
     * Prints that the game if finished and prints the ranking with users and their score
     * @param scores the list with the users and their score
     */
    @Override
    public void gameOver(List<Score> scores){
        ClientLogger.printlnWithClear("GAME FINISHED\n");
        if(scores.size() == 1)
            if(scores.get(0).getPlayer().equals(client.getGameSnapshot().getPlayer().getNickname()))
                ClientLogger.println("YOU WIN!");
        else
            for(Score score : scores)
                ClientLogger.println(score.getPlayer() + "   " + score.getTotalScore());
        wakeUpInput(null);
        wakeUp(true);
    }

    /**
     * Notifies that the server has responded to an action of the user and sets the result of the action
     * @param serverResult the result of the action
     */
    @Override
    public void wakeUp(boolean serverResult){
        this.serverResult = serverResult;
        flagContinue = true;

        if(waiting)
            if(waitLock2)
                synchronized (lock2) {
                    lock2.notifyAll();
                    waitLock2 = false;
                }
            else
                synchronized (lock1) {
                    lock1.notifyAll();
                }
    }

    /**
     * Notifies that the user has inserted something on the CLI and sets what the user wrote
     * @param inputResult what the user inserted on the CLI
     */
    void wakeUpInput(String inputResult){
        if(waitingInput) {
            this.inputResult = inputResult;
            flagContinueInput = true;
            synchronized (lockInput) {
                lockInput.notifyAll();
            }
        }
    }
}
