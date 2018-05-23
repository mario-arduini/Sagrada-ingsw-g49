package it.polimi.ingsw.network.client;

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

    private Client(){
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
                ClientLogger.println("Login failed, token is not correct");
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

    String askNickname(){
        String user = null;
        while(user == null) {
            ClientLogger.print("Insert your nickname: ");
            try {
                user = input.readLine();
                if(!checkNicknameProperties(user))
                    user = null;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.toString(), e);
                ClientLogger.println(INVALID_COMMAND);
                user = null;
            }
        }
        nickname = user;
        return user;
    }

    private boolean checkNicknameProperties(String user){
        return user != null && !user.equals("");
    }

    String askToken(){
        try {
            ClientLogger.print("Insert your token: ");
            return input.readLine();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
        return null;
    }

    void printToken(String token){
        ClientLogger.println("Your token is " + token);
    }

    private Connection createConnection(){
        if(connectionType == ConnectionType.SOCKET)
            return new ClientSocketHandler(this, serverAddress, serverPort);
        else if(connectionType == ConnectionType.RMI)
            return null;
        return null;
    }

    private void startGame(){

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
            ClientLogger.println("Users playing:");
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
