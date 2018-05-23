package it.polimi.ingsw.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientSocketHandler implements Connection {

    private static final Logger LOGGER = Logger.getLogger( ClientSocketHandler.class.getName() );

    private BufferedReader input;
    private PrintWriter output;
    private Socket socket;
    private Boolean flagWaitLogin = false;
    private String[] splitCommand;
    private Client client;
    private ServerListener serverListener;

    ClientSocketHandler(Client client, String serverAddress, int serverPort) {
        this.client = client;

        try {
            socket = new Socket(serverAddress, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            serverListener = new ServerListener(client, this);
            serverListener.start();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

    public synchronized boolean login() {

        socketPrintLine("login " + client.askNickname());

        while (!flagWaitLogin)
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, e.toString(), e);
            }

        if (splitCommand[2].equals("token")) {
            socketPrintLine("token " + client.askToken());

            while (flagWaitLogin)
                try {
                    wait();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                }
            return splitCommand[0].equals("verified");
        }
        client.printToken(splitCommand[2]);
        flagWaitLogin = false;
        return true;
    }

    synchronized void continueLogin(String[] splitCommand){
        flagWaitLogin = !flagWaitLogin;
        this.splitCommand = splitCommand;
        notifyAll();
    }

    public void logout(){
        socketPrintLine("logout");
        serverListener.setConnected(false);
        stopServerListener();
    }

    void stopServerListener(){
        serverListener.interrupt();
        socketClose();
    }

    private void socketPrintLine(String p) {
        output.println(p);
        output.flush();
    }

    String socketReadLine(){
        try {
            return input.readLine();
        }  catch(SocketException e){
            return null;
        } catch(IOException e) {
            //LOGGER.log(Level.WARNING, e.toString(), e);
        }
        return null;
    }

    private void socketClose(){
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }
}
