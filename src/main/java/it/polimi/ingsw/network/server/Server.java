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

/**
 * Server class, core of the server application, handles RMI and socket connections.
 * Spawns thread for each socket connection.
 */
public class Server {
    private int socketPort;
    private ExecutorService executor;
    private GamesHandler gamesHandler;
    private ServerSocket serverSocket;
    private boolean listening;

    /**
     * Constructor, initialized with socket port.
     * @param port port used for socket connections.
     */
    private Server(int port) {
        this.socketPort = port;
        this.gamesHandler = new GamesHandler();
        this.executor = Executors.newCachedThreadPool();
        this.listening = true;
    }

    /**
     * Initiate RMI registry and handles socket connections.
     * @throws IOException in case RMI port or Socket port are busy.
     */
    private void startServer() throws IOException {
        Login rmiLogger = new Login(gamesHandler);
        Socket clientSocket;

        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("logger", rmiLogger);
        Logger.print("Server: Rmi registry ready.");

        serverSocket = new ServerSocket(socketPort);
        Logger.print("Server: Socket listening on port " + socketPort + ".");
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

    /**
     * Method that initiate a Killer thread that will stop the server under certain conditions.
     */
    private void setKiller(){
        Thread killer = new Thread(new CliListener(this));
        killer.start();
    }

    /**
     * Method that interrupts socket connections and prepares server to halt.
     */
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
