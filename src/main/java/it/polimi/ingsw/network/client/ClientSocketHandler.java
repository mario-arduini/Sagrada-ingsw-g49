package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.polimi.ingsw.model.Coordinate;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.network.RMIInterfaces.FlowHandlerInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

public class ClientSocketHandler implements FlowHandlerInterface {

    private static final Logger LOGGER = Logger.getLogger( ClientSocketHandler.class.getName() );

    private transient PrintWriter output;
    private transient Socket socket;
    private transient Thread thread;
    private transient JsonObject jsonObject;
    private transient Gson gson;


    ClientSocketHandler(Client client, String serverAddress, int serverPort) throws SocketException {
        ClientLogger.initLogger(LOGGER);
        try {
            socket = new Socket(serverAddress, serverPort);
            output = new PrintWriter(socket.getOutputStream(), true);
            ServerListener serverListener = new ServerListener(client, this, socket);
            thread = new Thread(serverListener);
            thread.start();
        } catch (Exception e) {
            LOGGER.warning("Connection to server failed: " + e.toString());
            throw new SocketException();
        }
        gson = new Gson();
    }

    ClientSocketHandler login(String nickname, String password) {

        createJsonCommand("login");
        jsonObject.addProperty("nickname", nickname);
        jsonObject.addProperty("password", password);
        socketPrintLine(jsonObject);
        return this;
    }

    @Override
    public void chooseSchema(Integer schema){
        createJsonCommand("schema");
        jsonObject.addProperty("id", schema);
        socketPrintLine(jsonObject);
    }

    @Override
    public void placeDice(int row, int column, Dice dice){
        createJsonCommand("place-dice");
        jsonObject.addProperty("dice", gson.toJson(dice));
        jsonObject.addProperty("row", row);
        jsonObject.addProperty("column", column);
        socketPrintLine(jsonObject);
    }

    @Override
    public void useToolCard(String name){
        createJsonCommand("toolcard");
        jsonObject.addProperty("name", name);
        socketPrintLine(jsonObject);
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

    @Override
    public void newGame(){
        createJsonCommand("new-game");
        socketPrintLine(jsonObject);
    }

    private void stopServerListener(){
        socketClose();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.warning(e.toString());
        }
    }

    private void createJsonCommand(String command){
        jsonObject = new JsonObject();
        jsonObject.addProperty("command", command);
    }

    private void socketPrintLine(JsonObject jsonObject) {
        output.println(jsonObject);
        output.flush();
    }

    private void socketClose(){
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.warning(e.toString());
        }
    }

    //region TOOLCARD

    void sendPlusMinusOption(boolean choice){
        createJsonCommand("toolcard-plus-minus");
        jsonObject.addProperty("choice", choice);
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

    void sendDiceValue(int value) {
        createJsonCommand("toolcard-dice-value");
        jsonObject.addProperty("choice", value);
        socketPrintLine(jsonObject);
    }

    void sendRollback(){
        createJsonCommand("rollback");
        jsonObject.addProperty("choice", "rollback");
        socketPrintLine(jsonObject);
    }

    void sendMoveNumber(int n){
        createJsonCommand("move-dice-number");
        jsonObject.addProperty("choice", n);
        socketPrintLine(jsonObject);
    }

    //endregion
}
