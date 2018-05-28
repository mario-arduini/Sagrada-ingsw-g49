package it.polimi.ingsw.network.client;

import com.google.gson.JsonObject;

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
    private Client client;
    private ServerListener serverListener;
    private boolean flagContinue;
    private boolean connected;
    JsonObject jsonObject;


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
        flagContinue = false;
    }

    public synchronized boolean login() {

        createJsonCommand("login");
        jsonObject.addProperty("nickname", client.askNickname());
        jsonObject.addProperty("password", client.askPassword());

        socketPrintLine(jsonObject);

        while (!flagContinue)
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, e.toString(), e);
            }
        flagContinue = false;
        return connected;
    }

    synchronized void resultLogin(boolean result){
        flagContinue = true;
        connected = result;
        notifyAll();
    }

    public void sendSchema(int schema){
        createJsonCommand("schema");
        jsonObject.addProperty("schema", schema);
        socketPrintLine(jsonObject);
    }

    public void logout(){
        createJsonCommand("logout");
        socketPrintLine(jsonObject);
        serverListener.setConnected(false);
        stopServerListener();
    }

    private void stopServerListener(){
        serverListener.interrupt();
        socketClose();
    }

    private void createJsonCommand(String command){
        jsonObject = new JsonObject();
        jsonObject.addProperty("command", command);
    }

    private void socketPrintLine(JsonObject jsonObject) {
        output.println(jsonObject);
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
