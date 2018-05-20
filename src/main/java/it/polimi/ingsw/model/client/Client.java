package it.polimi.ingsw.model.client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private String nickname = null;
    private String serverAddress;
    private int serverPort;
    private BufferedReader input;
    private Connection server;
    private ConnectionType connectionType = null;
    private enum ConnectionType{ RMI, SOCKET }
    private List<String> players;
    private ServerListener serverListener;

    private Client(){
        players = new ArrayList<>();
        input = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("Insert server address: ");
            serverAddress = input.readLine();
            System.out.print("Insert server port: ");
            serverPort = Integer.parseInt(input.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(connectionType == null)
            connectionType = getConnectionType();
        server = getConnection();

        serverListener = new ServerListener(this, server);
        serverListener.start();
    }

    private ConnectionType getConnectionType(){
        System.out.println("Connection types:");
        System.out.println("[0] Socket");
        System.out.println("[1] RMI");
        System.out.print("Your choice: ");

        try {
            switch (input.readLine()) {
                case "0":
                    System.out.println("You chose Socket");
                    return ConnectionType.SOCKET;
                case "1":
                    System.out.println("You chose RMI");

                    return ConnectionType.RMI;
                default:
                    System.out.println("Invalid command");
                    return null;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Invalid command");
        return  null;
    }

    private String getNickname(){
        System.out.print("Insert your nickname: ");
        try {
            String user =  input.readLine();
            if(user.equals("logout")){
                System.out.println("Invalid command");
                return null;
            }
            return user;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Invalid command");
        return null;
    }

    private Connection getConnection(){
        if(connectionType == ConnectionType.SOCKET)
            return new ClientSocketHandler(serverAddress, serverPort);
        else if(connectionType == ConnectionType.RMI)
            return null;
        return null;
    }

    private boolean login(){
        while(nickname == null)
            nickname = getNickname();
        if(server.login(nickname)) {
            players.add(nickname);
            return true;
        }
        nickname = null;
        return  false;
    }

    private void startGame(){

    }

    private void logout(){
        String command = null;
        try {
            command = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(command.equals("logout")){
            serverListener.stop();
            server.logout();
        }
    }

    void addPlayer(String nickname){
        players.add(nickname);
    }

    void removePlayer(String nickname){
        players.remove(nickname);
    }

    public static void main(String[] args) {
        Boolean logged = false;
        Client client = new Client();
        while(!logged)
            logged = client.login();
        client.logout();
    }
}
