package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.Color;

import it.polimi.ingsw.model.Constraint;
import it.polimi.ingsw.model.Schema;
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
    private static final String CLI_SCHEMA_ROW = "---------------------    ---------------------";
    private static final int ROWS_NUMBER = 4;
    private static final int COLUMNS_NUMBER = 5;

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
    }

    synchronized void welcomePlayer(){
        ClientLogger.println("Welcome to Sagrada!");
        serverConnected = true;
        notifyAll();
    }

    private synchronized void start(){
        while(serverAddress == null)
            serverAddress = askServerAddress();

        while(serverPort == 0)
            serverPort = askServerPort();

        while(connectionType == null)
            connectionType = askConnectionType();

        while(server == null)
            server = createConnection();

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
                if(!checkPasswordProperties(password)) {
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

    void setPrivateGoal(String[] privateGoal){}

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

    void addPlayers(List<String> newPlayers){

        players.addAll(newPlayers);
        if(!logged){
            ClientLogger.println("Waiting room:");
            ClientLogger.println(nickname);
            newPlayers.forEach(ClientLogger::println);
        }
        else
            newPlayers.forEach(name -> ClientLogger.println(name + " is now playing"));
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

    private void print_schemas(List<Schema> schemas){
        Constraint constraint;
        Schema currentSchema;
        ClientLogger.println("Choose your schema:");
        for(int i=0;i<schemas.size();i+=2){
            ClientLogger.println("");
            ClientLogger.println("Schema ("+i+")               Schema ("+(i+1)+")");
            ClientLogger.println(CLI_SCHEMA_ROW);
            for(int r=0;r<ROWS_NUMBER;r++){
                currentSchema = schemas.get(i);
                for(int c=0;c<COLUMNS_NUMBER;c++){
                    constraint = currentSchema.getConstraint(r,c);
                    if(constraint==null) ClientLogger.print("|   ");
                    else if(constraint.getColor()!=null) ClientLogger.print("| "+ constraint.getColor().escape() +"■ "+Color.RESET);
                    else if(constraint.getNumber()!=0) ClientLogger.print("| "+constraint.getNumber()+" ");
                }
                ClientLogger.print("|    ");
                currentSchema = schemas.get(i+1);
                for(int c=0;c<COLUMNS_NUMBER;c++){
                    constraint = currentSchema.getConstraint(r,c);
                    if(constraint==null) ClientLogger.print("|   ");
                    else if(constraint.getColor()!=null) ClientLogger.print("| "+ constraint.getColor().escape() +"■ "+Color.RESET);
                    else if(constraint.getNumber()!=0) ClientLogger.print("| "+constraint.getNumber()+" ");
                }
                ClientLogger.println("|");
                ClientLogger.println(CLI_SCHEMA_ROW);
            }
            ClientLogger.println("Difficulty: "+schemas.get(i).getDifficulty()+"            Difficulty: "+schemas.get(i+1).getDifficulty());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
