package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.network.client.model.Coordinate;
import it.polimi.ingsw.network.client.model.Dice;
import it.polimi.ingsw.network.client.model.GameSnapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

public class ClientSocketHandler implements Connection {

    private static final Logger LOGGER = Logger.getLogger( ClientSocketHandler.class.getName() );

    private BufferedReader input;
    private PrintWriter output;
    private Socket socket;
    private ServerListener serverListener;
    private Thread thread;
    private boolean flagContinue;
    private boolean serverResult;
    private JsonObject jsonObject;
    private Gson gson;


    ClientSocketHandler(Client client, String serverAddress, int serverPort, GameSnapshot gameSnapshot) throws SocketException {
        ClientLogger.initLogger(LOGGER);
        try {
            socket = new Socket(serverAddress, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            serverListener = new ServerListener(client, this, gameSnapshot);
            thread = new Thread(serverListener);
            thread.start();
        } catch (Exception e) {
            LOGGER.warning("Connection to server failed: " + e.toString());
            throw new SocketException();
        }
        gson = new Gson();
        flagContinue = false;
    }

    @Override
    public synchronized boolean login(String nickname, String password) {

        createJsonCommand("login");
        jsonObject.addProperty("nickname", nickname);
        jsonObject.addProperty("password", password);

        socketPrintLine(jsonObject);

        while (!flagContinue)
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }

        flagContinue = false;
        return serverResult;
    }

    synchronized void notifyResult(boolean result){
        serverResult = result;
        flagContinue = true;
        notifyAll();
    }

    @Override
    public synchronized void sendSchema(int schema){
        //JsonParser parser = new JsonParser();
        createJsonCommand("schema");
        jsonObject.addProperty("id", schema);
        socketPrintLine(jsonObject);

        //jsonObject = parser.parse(socketReadLine()).getAsJsonObject();
        //return jsonObject.get("message").getAsString().equals("verified");
    }

    @Override
    public synchronized boolean placeDice(Dice dice, int row, int column){
        createJsonCommand("place-dice");
        jsonObject.addProperty("dice", gson.toJson(dice));
        jsonObject.addProperty("row", row - 1);
        jsonObject.addProperty("column", column - 1);
        socketPrintLine(jsonObject);

        while (!flagContinue)
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        flagContinue = false;
        return serverResult;
    }

    @Override
    public synchronized boolean useToolCard(String name) {
        createJsonCommand("toolcard");
        jsonObject.addProperty("name", name);
        socketPrintLine(jsonObject);

        while (!flagContinue)
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        flagContinue = false;
        return serverResult;
    }

    @Override
    public void pass(){
        createJsonCommand("pass");
        socketPrintLine(jsonObject);
    }

    @Override
    public void logout(){
        createJsonCommand("logout");
        socketPrintLine(jsonObject);
        stopServerListener();
    }

    private void stopServerListener(){
        serverListener.setConnected(false);
        thread.interrupt();
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
        } catch(Exception e) {
            LOGGER.warning(e.toString());
        }
        return null;
    }

    private void socketClose(){
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.warning(e.toString());
        }
    }

    //region TOOLCARD

    void sendPlusMinusOption(String choice){
        createJsonCommand("toolcard-plus-minus");
        jsonObject.addProperty("choice", choice.equals("+")||choice.equals("y"));
        socketPrintLine(jsonObject);
    }

    void sendDiceFromDraftPool(Dice dice){
        createJsonCommand("toolcard-dice-draftpool");
        jsonObject.addProperty("choice", gson.toJson(dice));
        socketPrintLine(jsonObject);
    }

    void sendDiceFromRoundTrack(int index){
        createJsonCommand("toolcard-dice-roundtrack");
        jsonObject.addProperty("choice", index);
        socketPrintLine(jsonObject);
    }

    void sendDiceFromWindow(Coordinate coordinate){
        createJsonCommand("toolcard-dice-window");
        jsonObject.addProperty("choice", gson.toJson(coordinate));
        socketPrintLine(jsonObject);
    }

    void sendPlacementPosition(Coordinate coordinate){
        createJsonCommand("toolcard-place-window");
        jsonObject.addProperty("choice", gson.toJson(coordinate));
        socketPrintLine(jsonObject);
    }

    void sendDiceValue(int value) {
        createJsonCommand("toolcard-dice-value");
        jsonObject.addProperty("choice", value);
        socketPrintLine(jsonObject);
    }

    //endregion
}
