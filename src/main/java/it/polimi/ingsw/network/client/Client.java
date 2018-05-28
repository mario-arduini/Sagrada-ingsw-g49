package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.*;

public class Client {

    private static final Logger LOGGER = Logger.getLogger( Client.class.getName() );
    private static final String INVALID_COMMAND = "Invalid command";

    private String nickname;
    private String serverAddress;
    private int serverPort;
    private BufferedReader input;
    private Connection server;
    private enum ConnectionType{ RMI, SOCKET }
    private ConnectionType connectionType;
    private List<String> players;
    private boolean logged;
    private boolean serverConnected;

    public Client(){
        players = new ArrayList<>();
        input = new BufferedReader(new InputStreamReader(System.in));

        while(serverAddress == null)
            serverAddress = askServerAddress();

        while(serverPort == 0)
            serverPort = askServerPort();

        while(connectionType == null)
            connectionType = askConnectionType();

        while(server == null)
            server = createConnection();
    }

    synchronized void welcomePlayer(){
        ClientLogger.println("Welcome to Sagrada!");
        serverConnected = true;
        notifyAll();
    }

    private synchronized void start(){
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
        logout();
    }

    private String askServerAddress(){
        String address;
        ClientLogger.print("Insert server address: ");
        try {
            address = input.readLine();
            if(address.equals("") || address.equals(" ") || address.contains(" ")) {
                ClientLogger.println("Invalid server address");
                return null;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            ClientLogger.println(INVALID_COMMAND);
            return null;
        }
        return  address;
    }

    private int askServerPort(){

        int port;
        ClientLogger.print("Insert server port: ");
        try {
            port = Integer.parseInt(input.readLine());
            if(port < 1000 || port > 65535) {
                ClientLogger.println("Invalid server port");
                return  0;
            }
        }
        catch (NumberFormatException e){
            ClientLogger.println("Server port must be a number");
            return 0;
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            ClientLogger.println(INVALID_COMMAND);
            return 0;
        }
        return port;
    }

    private ConnectionType askConnectionType(){
        ClientLogger.println("Connection types:");
        ClientLogger.println("[0] Socket");
        ClientLogger.println("[1] RMI");
        ClientLogger.print("Your choice: ");

        try {
            switch (input.readLine()) {
                case "0":
                    ClientLogger.println("You chose Socket");
                    return ConnectionType.SOCKET;
                case "1":
                    ClientLogger.println("You chose RMI");
                    return ConnectionType.RMI;
                default:
                    ClientLogger.println(INVALID_COMMAND);
                    return null;
            }
        }
        catch (IOException e){
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
        ClientLogger.println(INVALID_COMMAND);
        return  null;
    }

    private Connection createConnection(){
        if(connectionType == ConnectionType.SOCKET)
            return new ClientSocketHandler(this, serverAddress, serverPort);
        else if(connectionType == ConnectionType.RMI)
            return null;
        return null;
    }

    String askNickname(){
        String user = null;
        while(user == null) {
            ClientLogger.print("Insert your nickname: ");
            try {
                user = input.readLine();
                if(!checkNicknameProperties(user)) {
                    user = null;
                    ClientLogger.println("Invalid nickname");
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.toString(), e);
                ClientLogger.println(INVALID_COMMAND);
                user = null;
            }
        }
        nickname = user;
        return user;
    }

    private boolean checkPasswordProperties(String password){
        return password != null && !password.equals("") && password.length() >= 8;
    }

    private boolean checkNicknameProperties(String user){
        return user != null && !user.equals("");
    }

    String askPassword(){
        String password = null;
        while(password == null) {
            ClientLogger.print("Insert your password: ");
            try {
                password = input.readLine();
                if(!checkNicknameProperties(password)) {
                    password = null;
                    ClientLogger.println("Invalid nickname, must be at least 8 character");
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.toString(), e);
                ClientLogger.println(INVALID_COMMAND);
                password = null;
            }
        }
        return password;

    }
//    String askToken(){
//        try {
//            ClientLogger.print("Insert your token: ");
//            return input.readLine();
//        } catch (IOException e) {
//            LOGGER.log(Level.WARNING, e.toString(), e);
//        }
//        return null;
//    }
//
//    void printToken(String token){
//        ClientLogger.println("Your token is " + token);
//    }

    void setPrivateGoal(String[] privateGoal){}

    public void showSchema(char[][] schema, int difficulty){
        ClientLogger.println("");
        ClientLogger.println("Questa finestra ha difficoltà " + difficulty);
        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                switch (schema[i][j]){
                    case 'R':
                        ClientLogger.print(Color.RED.escape() + "[R] " + Color.RESET);
                        break;
                    case 'G':
                        ClientLogger.print(Color.GREEN.escape() + "[G] " + Color.RESET);
                        break;
                    case 'Y':
                        ClientLogger.print(Color.YELLOW.escape() + "[Y] " + Color.RESET);
                        break;
                    case 'P':
                        ClientLogger.print(Color.PURPLE.escape() + "[P] " + Color.RESET);
                        break;
                    case 'B':
                        ClientLogger.print(Color.BLUE.escape() + "[B] " + Color.RESET);
                        break;
                    case '1':
                        ClientLogger.print("[1] ");
                        break;
                    case '2':
                        ClientLogger.print("[2] ");
                        break;
                    case '3':
                        ClientLogger.print("[3] ");
                        break;
                    case '4':
                        ClientLogger.print("[4] ");
                        break;
                    case '5':
                        ClientLogger.print("[5] ");
                        break;
                    case '6':
                        ClientLogger.print("[6] ");
                        break;
                    default:
                        ClientLogger.print("[ ] ");
                        break;
                }
            }
            ClientLogger.println("");
        }
    }

    private void logout(){
        try {
            String command = input.readLine();
            if(command.equals("logout")){
                server.logout();
                ClientLogger.println("Logged out");
                logged = false;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

    private boolean isLogged(){
        return logged;
    }

    void addPlayers(String[] newPlayers){

        players.addAll(Arrays.asList(newPlayers));
        if(!logged){
            ClientLogger.println("Wainting room:");
            ClientLogger.println(nickname);
            Arrays.stream(newPlayers).forEach(ClientLogger::println);
        }
        else
            Arrays.stream(newPlayers).forEach(name -> ClientLogger.println(name + " is now playing"));
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

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
