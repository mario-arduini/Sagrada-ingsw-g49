package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GamesHandler;
import it.polimi.ingsw.network.server.rmi.Login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int port;
    private ExecutorService executor;
    private GamesHandler gamesHandler;
    private ServerSocket serverSocket;
    private boolean listening;
    private Thread killer;

    private Server(int port) {
        this.port = port;
        this.gamesHandler = new GamesHandler();
        this.executor = Executors.newCachedThreadPool();
        this.listening = true;
    }

    private void startServer() throws IOException {
        Login rmiLogger = new Login(gamesHandler);
        Socket clientSocket;

        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("logger", rmiLogger);
        Logger.print("Server: Rmi registry ready.");

        serverSocket = new ServerSocket(port);
        Logger.print("Server: Socket listening on port " + port + ".");
        setKiller();
        while(listening) {
            try {
                clientSocket = serverSocket.accept();
                Logger.print("Connection over socket: " + clientSocket.getRemoteSocketAddress().toString());
                executor.submit(new SocketHandler(clientSocket, gamesHandler));
            } catch (IOException e) {
                Logger.print(e.getMessage());
            }
        }

        executor.shutdown();
    }

    private void setKiller(){
        killer = new Thread(new CliListener(this));
        killer.start();
    }

    public void stop(){
        Logger.print("Shutting down.");
        this.listening=false;
        try {
            serverSocket.close();
        } catch (Exception e) {
        }
    }

    public static void main(String[] args){
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        int port = 0;
        while (port == 0)
            try {
                System.out.print("Enter a port: ");
                port = Integer.parseInt(buffer.readLine());
                if (port < 10000 || port > 65355){
                    System.out.println("Valid port number above 10000 and below 65355.");
                    port = 0;
                }else {
                    Server server = new Server(port);
                    server.startServer();
                    server.executor.shutdown();
                    System.exit(0);
                }
            }catch (NumberFormatException e){
                System.out.println("Insert a number.");
                port = 0;
            }catch (IOException e){
                System.out.println("Not a valid port number.");
                port = 0;
            }
    }

}
