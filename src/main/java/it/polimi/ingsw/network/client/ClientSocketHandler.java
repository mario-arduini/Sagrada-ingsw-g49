package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.polimi.ingsw.model.Coordinate;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.network.RmiInterfaces.FlowHandlerInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

/**
 * Handles all the socket communication from the client to the server
 */
public class ClientSocketHandler implements FlowHandlerInterface {

    private static final Logger LOGGER = Logger.getLogger( ClientSocketHandler.class.getName() );

    private transient PrintWriter output;
    private transient Socket socket;
    private transient Thread thread;
    private transient JsonObject jsonObject;
    private transient Gson gson;


    /**
     * Creates an object to handle the communication from client to server
     * Start a thread that always listens what the server sends via socket
     * @param client the object where the thread will notify the client about what the server sends
     * @param serverAddress the IP address of the server
     * @param serverPort the port of the server
     * @throws SocketException RMI exception
     */
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

    /**
     * Sends to the server a request to log in the user
     * @param nickname the nickname the user chose
     * @param password the password the user chose
     * @return itself
     */
    ClientSocketHandler login(String nickname, String password) {
        createJsonCommand("login");
        jsonObject.addProperty("nickname", nickname);
        jsonObject.addProperty("password", password);
        socketPrintLine(jsonObject);
        return this;
    }

    /**
     * Sends to the server the schema the user chose
     * @param schema the number of the schema the user chose
     */
    @Override
    public void chooseSchema(Integer schema){
        createJsonCommand("schema");
        jsonObject.addProperty("id", schema);
        socketPrintLine(jsonObject);
    }

    /**
     * Sends to the server a request to place a dice in a certain cell of the window
     * @param row the row of the cell chosen
     * @param column the column of the cell chosen
     * @param dice the dice chosen to be placed
     */
    @Override
    public void placeDice(int row, int column, Dice dice){
        createJsonCommand("place-dice");
        jsonObject.addProperty("dice", gson.toJson(dice));
        jsonObject.addProperty("row", row);
        jsonObject.addProperty("column", column);
        socketPrintLine(jsonObject);
    }

    /**
     * Sends to the server a request to use a certain tool card
     * @param name the name of the tool card chosen by the user
     */
    @Override
    public void useToolCard(String name){
        createJsonCommand("toolcard");
        jsonObject.addProperty("name", name);
        socketPrintLine(jsonObject);
    }

    /**
     * Sends to the server a request to continue the use of a tool card already begun before a disconnection
     */
    @Override
    public void continueToolCard(){
        createJsonCommand("continue-toolcard");
        socketPrintLine(jsonObject);
    }

    /**
     * Notifies to the server that the user wants to pass his turn
     */
    @Override
    public void pass(){
        createJsonCommand("pass");
        socketPrintLine(jsonObject);
    }

    /**
     * Notifies to the server that the user wants to log out from the game
     */
    @Override
    public void logout(){
        createJsonCommand("logout");
        socketPrintLine(jsonObject);
        stopServerListener();
    }

    /**
     * Sends to the server a request to start a new game
     */
    @Override
    public void newGame(){
        createJsonCommand("new-game");
        socketPrintLine(jsonObject);
    }

    /**
     * Stops the thread that always listen what the server sends via socket
     */
    private void stopServerListener(){
        socketClose();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.warning(e.toString());
        }
    }

    /**
     * Creates a Json object with a given attribute
     * @param command the attribute to add to the Json object
     */
    private void createJsonCommand(String command){
        jsonObject = new JsonObject();
        jsonObject.addProperty("command", command);
    }

    /**
     * Sends to the server, via socket, a Json object
     * @param jsonObject the Json object to send to the server
     */
    private void socketPrintLine(JsonObject jsonObject) {
        output.println(jsonObject);
        output.flush();
    }

    /**
     * Closes the communication via socket between the server and the client
     */
    private void socketClose(){
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.warning(e.toString());
        }
    }

    //region TOOLCARD

    /**
     * Sends to the server if the user wants to increment or decrement the value of the dice
     * @param choice is true if user wants to the increment the value of the dice, false otherwise
     */
    void sendPlusMinusOption(boolean choice){
        createJsonCommand("toolcard-plus-minus");
        jsonObject.addProperty("choice", choice);
        socketPrintLine(jsonObject);
    }

    /**
     * Sends to the server which dice the user chose from the draft pool
     * @param dice the dice the user chose from the draft pool
     */
    void sendDiceFromDraftPool(Dice dice){
        createJsonCommand("toolcard-dice-draftpool");
        jsonObject.addProperty("choice", gson.toJson(dice));
        socketPrintLine(jsonObject);
    }

    /**
     * Sends to the server which dice the user chose from the round track
     * @param index the index of the dice the user chose from the round track
     */
    void sendDiceFromRoundTrack(int index){
        createJsonCommand("toolcard-dice-roundtrack");
        jsonObject.addProperty("choice", index);
        socketPrintLine(jsonObject);
    }

    /**
     * Sends to the server which cell the user chose from the window
     * @param coordinate the coordinates of the cell the user chose from the window
     */
    void sendDiceFromWindow(Coordinate coordinate){
        createJsonCommand("toolcard-dice-window");
        jsonObject.addProperty("choice", gson.toJson(coordinate));
        socketPrintLine(jsonObject);
    }

    /**
     * Sends to the server the value the user chose for the dice
     * @param value the value the user chose for the dice
     */
    void sendDiceValue(int value) {
        createJsonCommand("toolcard-dice-value");
        jsonObject.addProperty("choice", value);
        socketPrintLine(jsonObject);
    }

    /**
     * Notifies the server that the user doesn't want to use the tool card anymore
     */
    void sendRollback(){
        createJsonCommand("rollback");
        jsonObject.addProperty("choice", "rollback");
        socketPrintLine(jsonObject);
    }

    /**
     * Sends to the server the number of dices the user wants to move
     * @param number the number of dices the user wants to move
     */
    void sendMoveNumber(int number){
        createJsonCommand("move-dice-number");
        jsonObject.addProperty("choice", number);
        socketPrintLine(jsonObject);
    }

    //endregion
}
