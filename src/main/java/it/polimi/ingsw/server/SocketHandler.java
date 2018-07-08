package it.polimi.ingsw.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.server.controller.GameFlowHandler;
import it.polimi.ingsw.server.controller.GamesHandler;
import it.polimi.ingsw.server.controller.exceptions.*;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.RmiInterfaces.ClientInterface;
import it.polimi.ingsw.server.exception.LoginFailedException;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

/**
 * Class that handles the interaction between the server and each client.
 * It is good practice to create a Socket Handler for each socket connection.
 */
public class SocketHandler implements Runnable, ClientInterface {
    private transient Socket socket;
    private transient BufferedReader input;
    private transient PrintWriter output;
    private String nickname;
    private boolean connected;
    private GameFlowHandler gameFlowHandler;
    private transient GamesHandler gamesHandler;
    private transient Gson gson;
    private transient JsonParser parser;
    private static final String NICKNAME_STRING = "nickname";

    /**
     * Build a new SocketHandler.
     * @param socket socket to which communicate.
     * @param gamesHandler reference to the handler of every gameFlow.
     */
    SocketHandler(Socket socket, GamesHandler gamesHandler) {
        this.socket = socket;
        this.gameFlowHandler = null;
        this.gamesHandler = gamesHandler;
        this.parser = new JsonParser();
        this.gson = new Gson();
        this.connected = true;
    }

    /**
     * Handles the interaction between a single client and a server, terminates if a disconnection happens.
     */
    public void run(){
        JsonObject message;
        String command;

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e1) {
            Logger.print("Socket Connection: input " + e1.getMessage());
        }
        try {
            output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e1) {
            Logger.print("Socket Connection: output " + e1.getMessage());
        }

        while (connected && !this.login());

        while(connected){

            try {
                message = socketReadCommand();

                command = getJsonStringValue(message, "command");

                if (command != null)
                    switch (command.toLowerCase()) {
                        case "logout":
                            logout();
                            break;
                        case "schema":
                            chooseSchema(message);
                            break;
                        case "place-dice":
                            placeDice(message);
                            break;
                        case "pass":
                            pass();
                            break;
                        case "toolcard":
                            useToolCard(message);
                            break;
                        case "continue-toolcard":
                            continueToolCard();
                            break;
                        case "new-game":
                            gameFlowHandler.newGame();
                            break;
                        default:
                            break;
                    }
            }catch (NullPointerException e){
                Logger.print("Disconnected: " + nickname + " " + socket.getRemoteSocketAddress().toString());
                this.gameFlowHandler.disconnected();
                connected = false;
            }
        }

        socketClose();
    }

    /**
     * Returns a String value from a JsonObject with a specific key.
     * @param message JsonObject message to examine.
     * @param key key value to look for.
     * @return String message or null otherwise.
     */
    private String getJsonStringValue(JsonObject message, String key){
        try {
            return message.get(key).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets value as a positive int from a json.
     * @param message JsonObject message to examine.
     * @param key key value to look for.
     * @return Positive int value. -1 if input is not kind.
     */
    private int getJsonPositiveIntValue(JsonObject message, String key){
        int i;
        try {
            i = message.get(key).getAsInt();
        }catch (Exception e){
            return -1;
        }
        if (i<0) return -1;
        return i;
    }

    /**
     * Private method that wraps GameFlowHandler's method.
     * Handles Exceptions and sends failure message if necessary.
     */
    private void placeDice(JsonObject message){
        try{
            gameFlowHandler.placeDice(getJsonPositiveIntValue(message, "row"), getJsonPositiveIntValue(message, "column"), gson.fromJson(message.get("dice").getAsString(), Dice.class));
        } catch (Exception e){
            Logger.print("Place Dice: " + e);
            socketSendMessage(createErrorMessage(e.toString()));
        }
    }

    /**
     * Private method that wraps GameFlowHandler's method.
     * Handles Exceptions and sends failure message if necessary.
     */
    private void pass(){
        try{
            gameFlowHandler.pass();
        }catch (Exception e){
            Logger.print("Pass: " + e);
        }
    }

    /**
     * Private method that wraps GameFlowHandler's method.
     * Handles Exceptions and sends schema again if something went wrong.
     */
    private void chooseSchema(JsonObject message){
        try {
            gameFlowHandler.chooseSchema(getJsonPositiveIntValue(message, "id"));
        } catch (IndexOutOfBoundsException | InvalidParameterException |
                WindowAlreadySetException | GameNotStartedException |
                GameOverException e){
            Logger.print(e);
            notifySchemas(gameFlowHandler.getInitialSchemas());
        }
    }

    /**
     * Private method that wraps GameFlowHandler's method.
     * Closes the socket connection and set the conditions to stop the thread.
     */
    private void logout(){
        gameFlowHandler.logout();
        connected = false;
        socketClose();
    }

    /**
     * Private method that wraps GameFlowHandler's method.
     * Handles Exceptions and sends failure message if necessary.
     */
    private void useToolCard(JsonObject message){
        try {
            gameFlowHandler.useToolCard(message.get("name").getAsString());
        } catch (InvalidParameterException | NoSuchToolCardException |
                NoDiceInWindowException | NothingCanBeMovedException |
                InvalidFavorTokenNumberException | AlreadyDraftedException |
                NotEnoughFavorTokenException | NotYourSecondTurnException |
                NoDiceInRoundTrackException | NotYourTurnException |
                NoSameColorDicesException | PlayerSuspendedException |
                NotDraftedYetException | NotYourFirstTurnException |
                GameNotStartedException | GameOverException |
                ToolcardAlreadyUsedException | NotEnoughDiceToMoveException |
                ToolCardInUseException e) {
            Logger.print("Toolcard : " + nickname + " " + e);
            socketSendMessage(createErrorMessage(e.toString()));
        }
    }

    /**
     * Private method that wraps GameFlowHandler's method.
     * Handles Exceptions and sends failure message if necessary.
     */
    private void continueToolCard(){
        try {
            gameFlowHandler.continueToolCard();
        } catch (InvalidParameterException | NoSuchToolCardException |
                InvalidFavorTokenNumberException |
                NotEnoughFavorTokenException | NotYourTurnException | PlayerSuspendedException |
                GameNotStartedException | GameOverException |
                ToolcardAlreadyUsedException e) {
            Logger.print("Toolcard: continuing " + nickname + " " + e);
            socketSendMessage(createErrorMessage(e.toString()));
        }
    }

    @Override
    public void notifyLogin(String nickname) {
        JsonObject message;
        message = createMessage("new-player");
        message.addProperty(NICKNAME_STRING, nickname);
        socketSendMessage(message);
    }

    @Override
    public void notifyLogin(List<String> nicknames){
        JsonObject message;
        message = createMessage("game-room");
        message.addProperty("players", gson.toJson(nicknames));
        socketSendMessage(message);
    }

    @Override
    public void notifyLogout(String nickname){
        JsonObject message;
        message = createMessage("quit");
        message.addProperty(SocketHandler.NICKNAME_STRING, nickname);
        socketSendMessage(message);
    }

    @Override
    public void notifySchemas(List<Schema> schemas){
        JsonObject message;
        message = createMessage("schema-choice");
        message.addProperty("schemas", gson.toJson(schemas));
        socketSendMessage(message);
    }

    @Override
    public void notifyGameInfo(Map<String, Boolean> toolCards, List<String> publicGoals, String privateGoal){
        JsonObject message;
        message = createMessage("game-info");

        message.addProperty("toolcards", gson.toJson(toolCards));
        message.addProperty("public-goals", gson.toJson(publicGoals));
        message.addProperty("private-goal", privateGoal);

        socketSendMessage(message);
    }

    @Override
    public void notifyReconInfo(Map<String, Window> windows, Map<String, Integer> favorToken, List<Dice> roundTrack, String cardName){
        JsonObject message;
        message = createMessage("reconnect-info");
        message.addProperty("windows", gson.toJson(windows));
        message.addProperty("round-track", gson.toJson(roundTrack));
        message.addProperty("favor-token", gson.toJson(favorToken));
        message.addProperty("toolcard", cardName);
        socketSendMessage(message);
    }

    @Override
    public void notifyToolCardUse(String player, String toolcard, Window window, List<Dice> draftPool, List<Dice> roundTrack){
        JsonObject message;
        message = createMessage("toolcard-used");
        message.addProperty("player", player);
        message.addProperty("toolcard", toolcard);
        message.addProperty("window", gson.toJson(window));
        message.addProperty("draft-pool", gson.toJson(draftPool));
        message.addProperty("round-track", gson.toJson(roundTrack));
        socketSendMessage(message);
    }

    @Override
    public void notifyOthersSchemas(Map<String, Schema> playersSchemas){
        JsonObject message;
        message = createMessage("schema-chosen");
        message.addProperty("content", gson.toJson(playersSchemas));
        socketSendMessage(message);
    }

    @Override
    public void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundtrack){
        JsonObject message;
        message = createMessage("round");
        message.addProperty("player", currentPlayer);
        message.addProperty("draft-pool", gson.toJson(draftPool));
        message.addProperty("new-round", newRound);
        if (newRound)
            message.addProperty("round-track", gson.toJson(roundtrack));
        socketSendMessage(message);
    }

    @Override
    public void notifyDicePlaced(String nickname, int row, int column, Dice dice){
        JsonObject message;
        message = createMessage("update-window");
        message.addProperty(SocketHandler.NICKNAME_STRING, nickname);
        message.addProperty("row", row);
        message.addProperty("column", column);
        message.addProperty("dice", gson.toJson(dice));
        socketSendMessage(message);
    }

    @Override
    public void notifyEndGame(List<Score> scores){
        JsonObject message;
        message = createMessage("game-over");
        message.addProperty("scores", gson.toJson(scores));
        socketSendMessage(message);
    }

    @Override
    public void notifySuspension(String nickname){
        JsonObject message;
        message = createMessage("suspended");
        message.addProperty("player", nickname);
        socketSendMessage(message);
    }

    @Override
    public Coordinate askDiceWindow(String prompt, boolean rollback) throws RollbackException, DisconnectionException{
        JsonObject command;

        JsonObject toSend = createMessage("toolcard-dice-window");
        toSend.addProperty("prompt",prompt);
        toSend.addProperty("rollback", rollback);
        socketSendMessage(toSend);

        try{
            command = socketReadCommand();
            if (command.get("choice").getAsString().equalsIgnoreCase("rollback")){
                socketSendMessage(createMessage("rollback-ok"));
                throw new RollbackException();
            }

            return gson.fromJson(command.get("choice").getAsString(), Coordinate.class);

        } catch (NullPointerException e){
            Logger.print("Disconnection: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
            throw new DisconnectionException();
        }
    }

    @Override
    public Dice askDiceDraftPool(String prompt, boolean rollback) throws RollbackException, DisconnectionException{
        JsonObject command;

        JsonObject toSend = createMessage("toolcard-dice-draftpool");
        toSend.addProperty("prompt",prompt);
        toSend.addProperty("rollback", rollback);
        socketSendMessage(toSend);
        try{
            command = socketReadCommand();
            if (command.get("choice").getAsString().equalsIgnoreCase("rollback")){
                socketSendMessage(createMessage("rollback-ok"));
                throw new RollbackException();
            }

            return gson.fromJson(command.get("choice").getAsString(), Dice.class);

        } catch (NullPointerException e){
            Logger.print("Disconnection: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
            throw new DisconnectionException();
        }
    }

    @Override
    public int askDiceRoundTrack(String prompt, boolean rollback) throws RollbackException, DisconnectionException{
        JsonObject command;

        JsonObject toSend = createMessage("toolcard-dice-roundtrack");
        toSend.addProperty("prompt",prompt);
        toSend.addProperty("rollback", rollback);
        socketSendMessage(toSend);
        try{
            command = socketReadCommand();
            if (command.get("choice").getAsString().equalsIgnoreCase("rollback")){
                socketSendMessage(createMessage("rollback-ok"));
                throw new RollbackException();
            }

            return getJsonPositiveIntValue(command, "choice");

        } catch (NullPointerException e){
            Logger.print("Disconnection: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
            throw new DisconnectionException();
        }
    }

    @Override
    public boolean askIfPlus(String prompt, boolean rollback) throws RollbackException, DisconnectionException{
        JsonObject command;

        JsonObject toSend = createMessage("toolcard-plus-minus");
        toSend.addProperty("prompt",prompt);
        toSend.addProperty("rollback", rollback);
        socketSendMessage(toSend);
        try{
            command = socketReadCommand();
            if (command.get("choice").getAsString().equalsIgnoreCase("rollback")){
                socketSendMessage(createMessage("rollback-ok"));
                throw new RollbackException();
            }

            return command.get("choice").getAsBoolean();

        } catch (NullPointerException e){
            Logger.print("Disconnection: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
            throw new DisconnectionException();
        }

    }

    @Override
    public int askDiceValue(String prompt, boolean rollback) throws RollbackException, DisconnectionException{
        JsonObject command;

        JsonObject toSend = createMessage("toolcard-dice-value");
        toSend.addProperty("prompt",prompt);
        toSend.addProperty("rollback", rollback);
        socketSendMessage(toSend);
        try{
            command = socketReadCommand();
            if (command.get("choice").getAsString().equalsIgnoreCase("rollback")){
                socketSendMessage(createMessage("rollback-ok"));
                throw new RollbackException();
            }

            return getJsonPositiveIntValue(command, "choice");

        } catch (NullPointerException e){
            Logger.print("Disconnection: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
            throw new DisconnectionException();
        }
    }

    @Override
    public int askMoveNumber(String prompt, int n, boolean rollback) throws RollbackException, DisconnectionException{
        JsonObject command;

        JsonObject toSend = createMessage("move-dice-number");
        toSend.addProperty("prompt",prompt);
        toSend.addProperty("number", n);
        toSend.addProperty("rollback", rollback);
        socketSendMessage(toSend);
        try{
            command = socketReadCommand();
            if (command.get("choice").getAsString().equalsIgnoreCase("rollback")){
                socketSendMessage(createMessage("rollback-ok"));
                throw new RollbackException();
            }

            return getJsonPositiveIntValue(command, "choice");

        } catch (NullPointerException e){
            Logger.print("Disconnection: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
            throw new DisconnectionException();
        }
    }

    @Override
    public void showDice(Dice dice){
        JsonObject message;

        message = createMessage("show-dice");
        message.addProperty("dice", gson.toJson(dice));

        socketSendMessage(message);
    }

    @Override
    public void alertDiceInDraftPool(Dice dice){
        JsonObject message;

        message = createMessage("alert-dice");
        message.addProperty("dice", gson.toJson(dice));

        socketSendMessage(message);
    }

    /**
     * Method that performs login. After its usage nickname and gameflowhandler will be set.
     * @return true if login was successful, false otherwise.
     */
    private boolean login() {
        JsonObject command;
        String password;

        try {
            command = socketReadCommand();
            try{
                if (command.get("command").getAsString().equals("login")){
                    this.nickname = command.get(SocketHandler.NICKNAME_STRING).getAsString();
                    password = command.get("password").getAsString();
                    try{
                        gameFlowHandler = gamesHandler.login(nickname, password, this);
                        return true;
                    }catch (LoginFailedException | RemoteException e){
                        socketSendMessage(createErrorMessage(e.toString()));
                        return false;
                    }
                }
            }catch (NullPointerException e){
                this.nickname = null;
                Logger.print("Socket Connection Login: " + socket.getRemoteSocketAddress().toString() + " " + e.getMessage());
                socketSendMessage(createMessage("invalid-option"));
            }
        }catch (NullPointerException e){
            Logger.print("Disconnection before login: " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
        }
        return false;

    }

    /**
     * Sends a json object via socket.
     * @param json the object to be sent.
     */
    private void socketSendMessage(JsonObject json) {
        output.println(json);
        output.flush();
    }

    /**
     * Method used to read json messages from socket. NullPointerException if disconnection.
     * @return JsonObject containing the command or null if something went wrong.
     */
    private JsonObject socketReadCommand(){
        try {
            return parser.parse(socketReadLine()).getAsJsonObject();
        }
        catch (IllegalStateException e){
            Logger.print("Socket Connection: parsing Json " + e);
        }
        return null;
    }

    /**
     * Methods used to read from Socket.
     * @return String containing the message or null pointer in case something went wrong.
     */
    private String socketReadLine(){
        try {
            return input.readLine();
        } catch(IOException e) {
            Logger.print("Socket Connection: reading " + e.getMessage());
        }
            return null;
    }

    /**
     * Closes connection with socket.
     */
    private void socketClose(){
        try {
            socket.close();
        } catch (IOException e) {
            Logger.print("Socket Connection: closing " + e);
        }
    }

    /**
     * Creates a message of failure that includes a description of the error that occurred.
     * @param description description of failure.
     * @return message, JsonObject containing the message.
     */
    private static JsonObject createErrorMessage(String description){
        JsonObject message;
        message = createMessage("failed");
        message.addProperty("info", description);
        return message;
    }

    /**
     * Creates a standard message to be sent to the client.
     * @param message type of message.
     * @return JsonObject containing the message.
     */
    private static JsonObject createMessage(String message){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", message);
        return jsonObject;
    }
}
