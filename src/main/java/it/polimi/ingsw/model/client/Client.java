package it.polimi.ingsw.model.client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Client {

    private String nickname, serverAddress;
    private BufferedReader input;
    private Connection server;
    private ConnectionType connectionType;
    private enum ConnectionType{ RMI, SOCKET };

    public Client(){
        input = new BufferedReader(new InputStreamReader(System.in));
        connectionType = getConnectinType();
        nickname = getNickname();
        server = getConnection();
    }

    private ConnectionType getConnectinType(){
        System.out.println("Connection types:");
        System.out.println("[0] Socket");
        System.out.println("[1] RMI");
        System.out.print("Your choice: ");

        try {
            switch (input.readLine()) {
                case "0":
                    return ConnectionType.SOCKET;
                case "1":
                    return ConnectionType.RMI;
                default:
                    return null;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return  null;
    }

    private String getNickname(){
        System.out.print("Insert your nickname: ");
        try {
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Connection getConnection(){
        if(connectionType == ConnectionType.SOCKET)
            return new SocketHandler();
        else if(connectionType == ConnectionType.RMI)
            return null;
        return null;
    }

    private void login(){
        server.login(nickname);
    }

    private void startGame(){

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.login();
        client.startGame();
    }
}
