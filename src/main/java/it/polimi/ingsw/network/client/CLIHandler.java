package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.client.model.*;
import it.polimi.ingsw.network.client.model.Color;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

class CLIHandler implements GraphicInterface{

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
    private Object lock1;
    private boolean waitLock2;
    private Object lock2;

    private Object lockInput;

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
            command = readInt(0, 3);
            if (!client.isGameStarted())
                break;

            if (command > 0 && !client.getGameSnapshot().getPlayer().isMyTurn())
                ClientLogger.print(ERROR);
            else
                try {
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

    private void completeToolCard() throws ServerReconnectedException{
        ClientLogger.println("\nUsing the tool card " + toolCardNotCompleted + "\n");
        client.continueToolCard();
        waitResult(lock1);
    }

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

    private int askServerPort(){
        ClientLogger.print("Insert server port: ");
        return readInt(1000, 65535);
    }

    private Client.ConnectionType askConnectionType(){
        ClientLogger.printlnWithClear("Connection types:");
        ClientLogger.println("0) Socket");
        ClientLogger.println("1) RMI");
        ClientLogger.print("Your choice: ");

        if(readInt(0, 1) == 1)
            return Client.ConnectionType.RMI;
        return Client.ConnectionType.SOCKET;
    }

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

    private boolean checkNicknameProperties(String user){
        return user != null && !user.equals("");
    }

    private boolean checkPasswordProperties(String password){
        return password != null && !password.equals("") && password.length() >= 4;
    }

    @Override
    public void printWaitingRoom(){
        ClientLogger.printlnWithClear("Waiting for game to start");
        ClientLogger.println("\nWaiting room:");
        ClientLogger.println(client.getGameSnapshot().getPlayer().getNickname());
        client.getGameSnapshot().getOtherPlayers().forEach(nick -> ClientLogger.println(nick.getNickname()));
    }

    @Override
    public void printSchemaChoice(GameSnapshot gameSnapshot, List<Schema> schemas){
        ClientLogger.printlnWithClear("GAME STARTED!\n");
        printFooter(gameSnapshot);
        printSchemas(schemas);
    }


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
            if(!client.getServerConnected())
                if(waitResult(lock1))
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



    @Override
    public void notifyServerDisconnected(){
        ClientLogger.printlnWithClear("Server disconnected, trying to reconnect");
    }

    //region TOOLCARD

    @Override
    public void notifyUsedToolCard(String player, String toolCard){
        ClientLogger.println("\n" + player + " used the tool card " + toolCard);
    }

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

    @Override
    public Coordinate askDiceWindow(String prompt, boolean rollback) throws RollbackException {
        ClientLogger.println(MessageHandler.get(prompt));
        return getPosition(rollback);
    }

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

    @Override
    public void alertDiceInDraftPool(Dice dice){
        ClientLogger.println(MessageHandler.get("alert-dice"));
        printDice(dice);
    }

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

    //endregion

    public void setToolCardNotCompleted(String toolCard){
        this.toolCardNotCompleted = toolCard;
    }

    private static void printFooter(GameSnapshot gameSnapshot){
        printPublicGoals(gameSnapshot.getPublicGoals());
        printPrivateGoal(gameSnapshot.getPlayer().getPrivateGoal());
        printToolCards(gameSnapshot.getToolCards());
    }

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

        whiteSpaceNum = WINDOW_WIDTH - p.getNickname().length() - (p.isSuspended() ? 4 : 0) - 1 - p.getWindow().getSchema().getDifficulty();
        whiteSpaceHalf = whiteSpaceNum/2;

        for(i=0;i<whiteSpaceHalf;i++) ClientLogger.print(" ");

        ClientLogger.print(p.getNickname()+ (p.isSuspended() ? " (S) " : " "));
        for(i=0;i<p.getFavorToken();i++) ClientLogger.print("\u25CF");
        for(;i<p.getWindow().getSchema().getDifficulty();i++) ClientLogger.print("\u25CB");

        if(whiteSpaceNum%2 == 1) whiteSpaceHalf++;
        for(i=0;i<whiteSpaceHalf;i++) ClientLogger.print(" ");
    }

    private static void printPublicGoals(List<String> publicGoals){
        StringBuilder names = new StringBuilder();
        for(String name : publicGoals)
            names.append(" ").append(name).append(",");
        names.deleteCharAt(names.length() - 1);
        ClientLogger.println("PUBLIC GOAL:" + names + "\n");
    }

    private static void printPrivateGoal(String privateGoal){
        ClientLogger.println("PRIVATE GOAL: " + privateGoal);
    }

    private static void printToolCards(List<ToolCard> toolCards){
        int i = 0;
        ClientLogger.println("\nTOOL CARDS");
        for (ToolCard toolcard: toolCards) {
            ClientLogger.print(++i + ") " + toolcard.getName());
            for(int j = toolcard.getName().length();j<31;j++) ClientLogger.print(" ");
            ClientLogger.println("|  Cost: " + (toolcard.getUsed() ? "2" : "1"));
        }
    }

    @Override
    public void interruptInput(){
        wakeUpInput(null);
    }

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

    void wakeUpInput(String inputResult){
        if(waitingInput) {
            this.inputResult = inputResult;
            flagContinueInput = true;
            synchronized (lockInput) {
                lockInput.notifyAll();
            }
        }
    }

    @Override
    public void printDice(Dice dice){
        ClientLogger.println("Dice: " + dice);
    }
}
