package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameFlowHandler;
import it.polimi.ingsw.controller.GamesHandler;
import it.polimi.ingsw.network.server.rmi.Login;
import it.polimi.ingsw.network.server.rmi.LoginInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int port;
    private ExecutorService executor;
    private GamesHandler gamesHandler;

    private Server(int port) {
        this.port = port;
        this.gamesHandler = new GamesHandler();
        this.executor = Executors.newCachedThreadPool();
    }

    private void startServer() throws IOException {
        ServerSocket serverSocket;
        Socket clientSocket;
        LoginInterface rmiLogger = new Login(gamesHandler);

        Registry registry = LocateRegistry.getRegistry();
        registry.rebind("logger", rmiLogger);

        serverSocket = new ServerSocket(port);

        System.out.println("Server listening on port " + port + ".");

        while(true) {
            try {
                clientSocket = serverSocket.accept();
                Logger.print("Connection over socket: " + clientSocket.getRemoteSocketAddress().toString());
                executor.submit(new SocketHandler(clientSocket, new GameFlowHandler(gamesHandler)));
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
