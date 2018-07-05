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
    private String inputResult;
    private Thread thread;
    private CLIListener cliListener;

    CLIHandler() {
        ClientLogger.initLogger(LOGGER);
        flagContinue = false;
        waiting = false;
        newGame = true;
        waitingInput = false;
        flagContinueInput = false;

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
        }while (!client.createConnection(connectionType));

        waitResult();

        while(!ok) {
            ClientLogger.printWithClear("Welcome to Sagrada!\n\nChoose an option:\n0) Logout\n1) Login\nYour choice: ");
                command = readInt(0, 1);

            if(command == 0) {
                ClientLogger.printlnWithClear("Logged out");
                cliListener.stopListening();
                thread.interrupt();
                return;
            }
            client.login(askNickname(), askPassword());

            if(waitResult()) {
                client.setLogged();
                ok = true;
            }
            else
                ClientLogger.println("Login failed, password is not correct\n");
        }

        while (newGame) {
            if(!play())
                newGame = askNewGame();
            else
                newGame = false;
        }
        cliListener.stopListening();
        thread.interrupt();
    }

    private boolean play(){
        boolean logout = false;
        int command;
        if (!client.isGameStarted()) {
            waitResult();
            do {
                ClientLogger.print("\nYour choice: ");
                command = readInt(1, 4);
                if(command == -1)
                    return false;
                client.sendSchemaChoice(command - 1);
                if (!flagContinue)
                    ClientLogger.print("\nWaiting other players' choice");
                waitResult();
            } while (!serverResult);
        }

        if(!client.isGameStarted())
            return false;
        while (!logout) {
            command = readInt(0, 3);
            if (!client.isGameStarted())
                break;

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
        }
        return logout;
    }

    private boolean askNewGame(){
        ClientLogger.print("\nChoose an option:\n0) Logout\n1) New game\nYour choice: ");
        if(readInt(0, 1) == 1){
            client.newGame();
            waitResult();
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
        ClientLogger.printlnWithClear("Waiting for game to start, insert 0 to logout");
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


    private void placeDice(){
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
                    if(dice == -1)  return;
                    ClientLogger.print("Insert row: ");
                    row = readInt(1, ROWS);
                    if(row == -1)  return;
                    ClientLogger.print("Insert column: ");
                    column = readInt(1, COLUMNS);
                    if(column == -1)  return;
                }catch (CancellationException e){
                    return;
                }

                if (dice > client.getGameSnapshot().getDraftPool().size())
                    ClientLogger.println("Invalid choice!");
                else
                    ask = false;
            }
            client.placeDice(dice, row, column);
            if(!waitResult()) {
                printGame(client.getGameSnapshot());
                ClientLogger.println("\nConstraint violated!");
                printMenu(client.getGameSnapshot());
            } else
                client.verifyEndTurn();
        }else
            ClientLogger.println("Not your turn! You can only logout");
    }

    private void useToolCard() {
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
                if(choice == -1)  return;
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

        client.useToolCard(client.getGameSnapshot().getToolCards().get(choice - 1).getName());
        if (!waitResult()) {
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

    private synchronized boolean waitResult(){
        while (!flagContinue) {
            waiting = true;
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }
        waiting = false;
        flagContinue = false;
        return serverResult;
    }

    private synchronized String waitInput(){
        while (!flagContinueInput) {
            waitingInput = true;
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }
        waitingInput = false;
        flagContinueInput = false;
        return inputResult;
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
        ClientLogger.printlnWithClear("Server disconnected");
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
        for(int i=gameSnapshot.getRoundTrack().size();i<9;i++) ClientLogger.print("  \u25A1");

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
            synchronized (this) {
                this.notifyAll();
            }
    }

    void wakeUpInput(String inputResult){
        if(waitingInput) {
            this.inputResult = inputResult;
            flagContinueInput = true;
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    @Override
    public void printDice(Dice dice){
        ClientLogger.println("Dice: " + dice);
    }
}
