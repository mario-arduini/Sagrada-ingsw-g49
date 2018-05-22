package it.polimi.ingsw.model.client;

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
    private ConnectionType connectionType;
    private enum ConnectionType{ RMI, SOCKET }
    private List<String> players;
    private ServerListener serverListener;
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
        serverConnected = true;

        serverListener = new ServerListener(this, server);
        serverListener.start();
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

    private String askNickname(){
        String user;
        ClientLogger.print("Insert your nickname: ");
        try {
            user =  input.readLine();
            if(user.equals("logout")){
                ClientLogger.println("Invalid nickname");
                return null;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
            ClientLogger.println(INVALID_COMMAND);
            return null;
        }
        return user;
    }

    private Connection createConnection(){
        if(connectionType == ConnectionType.SOCKET)
            return new ClientSocketHandler(serverAddress, serverPort);
        else if(connectionType == ConnectionType.RMI)
            return null;
        return null;
    }

    private void login(){
        while(nickname == null)
            nickname = askNickname();
        if(server.login(nickname)) {
            players.add(nickname);
            logged = true;
            return;
        }
        nickname = null;
        logged = false;
    }

    private void startGame(){

    }

    private void logout(){
        try {
            String command = input.readLine();
            if(command.equals("logout")){
                server.logout();
                logged = false;
                serverListener.setConnected(false);
                serverListener.interrupt();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

    private boolean isLogged(){
        return logged;
    }

    private boolean isServerConnected(){
        return serverConnected;
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
            serverListener.setConnected(false);
            serverListener.interrupt();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        while(!client.isLogged() && client.isServerConnected())
            client.login();

        if(client.isServerConnected())
            client.logout();
    }
}
