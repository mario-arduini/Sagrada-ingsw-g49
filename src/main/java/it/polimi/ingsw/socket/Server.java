package it.polimi.ingsw.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static int PORT;
    //private static WaitingRoom waitingRoom;
    private ExecutorService executor;
    private UsersHandler usersHandler;

    public Server(int port) {
        this.PORT = port;
        //this.waitingRoom = WaitingRoom.getWaitingRoom();
        this.usersHandler = new UsersHandler();
        this.executor = Executors.newCachedThreadPool();
    }

    private void startServer() {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Server listening on port " + PORT + ".");

        while(1==1) {
            try {
                clientSocket = serverSocket.accept();
                executor.submit(new SocketHandler(clientSocket, usersHandler));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args){
        Server server = new Server(1337);
        server.startServer();
    }
}
