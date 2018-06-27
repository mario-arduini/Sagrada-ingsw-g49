package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.polimi.ingsw.controller.exceptions.GameNotStartedException;
import it.polimi.ingsw.controller.exceptions.GameOverException;
import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
import it.polimi.ingsw.model.Coordinate;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.RMIInterfaces.ClientInterface;
import it.polimi.ingsw.network.RMIInterfaces.FlowHandlerInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

public class ClientSocketHandler implements FlowHandlerInterface {

    private static final Logger LOGGER = Logger.getLogger( ClientSocketHandler.class.getName() );

    private BufferedReader input;
    private PrintWriter output;
    private Socket socket;
    private ServerListener serverListener;
    private Thread thread;
    private JsonObject jsonObject;
    private Gson gson;


    public ClientSocketHandler(ClientInterface client, String serverAddress, int serverPort) throws SocketException {
        ClientLogger.initLogger(LOGGER);
        try {
            socket = new Socket(serverAddress, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            serverListener = new ServerListener(client, this);
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
    public void chooseSchema(Integer schema) throws RuntimeException, GameNotStartedException, GameOverException, WindowAlreadySetException {
        createJsonCommand("schema");
        jsonObject.addProperty("id", schema);
        socketPrintLine(jsonObject);
    }

    @Override
    public void placeDice(int row, int column, Dice dice) throws RemoteException, GameOverException, NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException, NoSameColorDicesException, GameNotStartedException{
        createJsonCommand("place-dice");
        jsonObject.addProperty("dice", gson.toJson(dice));
        jsonObject.addProperty("row", row - 1);
        jsonObject.addProperty("column", column - 1);
        socketPrintLine(jsonObject);
    }

    @Override
    public void useToolCard(String name) throws RemoteException, GameNotStartedException, GameOverException, NoSuchToolCardException, InvalidDiceValueException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, NotWantedAdjacentDiceException, NoAdjacentDiceException, NotDraftedYetException, NotYourFirstTurnException, NothingCanBeMovedException, NoSameColorDicesException{
        createJsonCommand("toolcard");
        jsonObject.addProperty("name", name);
        socketPrintLine(jsonObject);
    }

    @Override
    public void pass() throws RemoteException, GameNotStartedException, GameOverException, NotYourTurnException{
        createJsonCommand("pass");
        socketPrintLine(jsonObject);
    }

    @Override
    public void logout() throws RemoteException{
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

    //endregion
}
