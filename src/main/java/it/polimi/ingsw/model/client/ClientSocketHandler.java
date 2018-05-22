package it.polimi.ingsw.model.client;

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
    private BufferedReader inputConsole;
    private PrintWriter output;
    private Socket socket;
    private Boolean flagLogin = false;
    private String[] splitCommand;

    ClientSocketHandler(String serverAddress, int serverPort) {
        inputConsole = new BufferedReader(new InputStreamReader(System.in));

        try {
            socket = new Socket(serverAddress, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            if (socketReadLine().equals("welcome"))
                ClientLogger.println("Welcome to Sagrada!");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.toString(), e);
        }
    }

    public synchronized boolean login(String nickname) {

        socketPrintLine("login " + nickname);

        while (!flagLogin)
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, e.toString(), e);
            }

        if (splitCommand[2].equals("token")) {
            ClientLogger.print("Insert your token: ");
            try {
                socketPrintLine("token " + inputConsole.readLine());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.toString(), e);
            }
            while (flagLogin)
                try {
                    wait();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, e.toString(), e);
                }

            switch (splitCommand[0]) {
                case "failed":
                    ClientLogger.println("Login failed, token is not correct");
                    return false;
                case "verified":
                    ClientLogger.println("Login successful, token is correct");
                    return true;
                default: return false;
            }

        }
        ClientLogger.println("Login successful, your token is " + splitCommand[2]);
        flagLogin = false;
        return true;
    }

    synchronized void continueLogin(String[] splitCommand){
        flagLogin = !flagLogin;
        this.splitCommand = splitCommand;
        notifyAll();
    }

    public void logout(){
        socketPrintLine("logout");
        socketClose();
        ClientLogger.println("Logged out");
    }

    private void socketPrintLine(String p) {
        output.println(p);
        output.flush();
    }

    String socketReadLine(){
        try {
            return input.readLine();
        }  catch (IOException e) {
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
