package it.polimi.ingsw.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
                    useToolcard();
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
        int choice = 0;
        boolean ok = false;

        while (!ok) {
            ClientLogger.println("Connection types:");
            ClientLogger.println("[0] Socket");
            ClientLogger.println("[1] RMI");
            ClientLogger.print("Your choice: ");

            try {
                choice = Integer.parseInt(input.readLine());
            } catch (IOException | NumberFormatException e) {
                ClientLogger.println("Not a valid choice");
            }
            if(choice != 0 && choice != 1)
                ClientLogger.println("Not a valid choice");
            else
                ok = true;
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
        String password = "notapassword";
        boolean ok = false;

        while (!ok) {
            ClientLogger.print("Insert your password: ");
            try {
                password = input.readLine();
            } catch (IOException e) {
                e.printStackTrace();
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
        return choice - 1;
    }

    void setPlayindRound(boolean playindRound){
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

    private void useToolcard(){
        if(client.isMyTurn()){

        }else
            ClientLogger.println("Not your turn! You can only logout");
    }

    boolean getPlayingRounf(){
        return playindRound;
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
}
