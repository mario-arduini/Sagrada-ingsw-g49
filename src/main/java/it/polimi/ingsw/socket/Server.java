package it.polimi.ingsw.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private void startServer() throws IOException {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        serverSocket = new ServerSocket(PORT);



        System.out.println("Server listening on port " + PORT + ".");

        while(true) {
            try {
                clientSocket = serverSocket.accept();
                executor.submit(new SocketHandler(clientSocket, usersHandler));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args){
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        int port = 0;
        while (port == 0)
            try {
                System.out.print("Enter a port: ");
                port = Integer.parseInt(buffer.readLine());
                if (port < 10000){
                    System.out.println("Valid port number above 10000");
                    port = 0;
                }else {
                    Server server = new Server(port);
                    server.startServer();
                }
            }catch (NumberFormatException e){
                System.out.println("Not a number");
            }catch (IOException e){
                System.out.println("Not a valid port number");
            }
    }
}
