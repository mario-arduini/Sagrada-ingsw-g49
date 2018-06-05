package it.polimi.ingsw.network.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.controller.GameFlowHandler;
import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.toolcards.ToolCard;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SocketHandler implements Runnable, ConnectionHandler{
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String nickname;
    private boolean connected;
    private GameFlowHandler gameFlowHandler;
    private Gson gson;
    private JsonParser parser;
    private static final String NICKNAME_STRING = "nickname";

    SocketHandler(Socket socket, GameFlowHandler gameFlowHandler) {
        this.socket = socket;
        this.gameFlowHandler = gameFlowHandler;
        this.parser = new JsonParser();
        this.gson = new Gson();
        this.connected = true;
    }

    public void run(){
        JsonObject message;
        String command;

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        message = createMessage("welcome");
        socketSendMessage(message);


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
                        //debugging
                        case "players":
                            List<String> players = gameFlowHandler.getPlayers();
                            socketPrintLine(players.toString());
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
                    }
            }catch (NullPointerException e){
                Logger.print("Disconnected: " + nickname + " " + socket.getRemoteSocketAddress().toString());
                this.gameFlowHandler.disconnected();
                connected = false;
            }
        }

        socketClose();
    }

    private String getJsonStringValue(JsonObject message, String key){
        try {
            return message.get(key).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private int getJsonPositiveIntValue(JsonObject message, String key) throws InvalidParameterException{
        int i;
        try {
            i = message.get(key).getAsInt();
        }catch (Exception e){
            return -1;
        }
        if (i<0) throw new InvalidParameterException();
        return i;
    }

    private void placeDice(JsonObject message){
        try{
            gameFlowHandler.placeDice(getJsonPositiveIntValue(message, "row"), getJsonPositiveIntValue(message, "column"), gson.fromJson(message.get("dice").getAsString(), Dice.class));
            socketSendMessage(createMessage("verified"));
            gameFlowHandler.notifyDicePlaced(getJsonPositiveIntValue(message, "row"), getJsonPositiveIntValue(message, "column"), gson.fromJson(message.get("dice").getAsString(), Dice.class));
            //TODO: separe Exceptions and send appropriate failure message
        } catch (Exception e){
            Logger.print(e);
            socketSendMessage(createMessage("failed"));
        }
    }

    private void pass(){
        try{
            gameFlowHandler.pass();
        }catch (Exception e){
            Logger.print(e);
        }
    }

    private void chooseSchema(JsonObject message){
        try {
            gameFlowHandler.chooseSchema(getJsonPositiveIntValue(message, "id"));
            socketSendMessage(createMessage("verified"));
            gameFlowHandler.checkGameReady();
        } catch (IndexOutOfBoundsException | InvalidParameterException e){
            socketSendMessage(createMessage("failed"));
        }
    }

    private void logout(){
        gameFlowHandler.logout();
        connected = false;
        socketClose();
    }

    private void useToolCard(JsonObject message){
        try {
            gameFlowHandler.useToolCard(message.get("name").getAsString());
            socketSendMessage(createMessage("verified"));
        } catch (InvalidParameterException | NoSuchToolCardException |
                InvalidDiceValueException | NoDiceInWindowException |
                InvalidFavorTokenNumberException | AlreadyDraftedException |
                NotEnoughFavorTokenException | NotYourSecondTurnException |
                NoDiceInRoundTrackException | NotYourTurnException |
                ConstraintViolatedException | NotWantedAdjacentDiceException |
                FirstDiceMisplacedException |
                BadAdjacentDiceException | NoAdjacentDiceException e) {
            Logger.print("Toolcard :" + nickname + e);
            socketSendMessage(createMessage("failed"));
        }
    }

    @Override
    public void notifyLogin(String nickname) {
        JsonObject message;
        message = createMessage("new_player");
        List <String> nicks = new ArrayList<>();
        nicks.add(nickname);
        message.addProperty("nicknames", gson.toJson(nicks));
        socketSendMessage(message);
    }

    @Override
    public void notifyLogin(List<String> nicknames){
        JsonObject message;
        message = createMessage("new_player");
        message.addProperty("nicknames", gson.toJson(nicknames));
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
        for (Integer i = 0; i < schemas.size(); i++)
            message.addProperty(i.toString(), gson.toJson(schemas.get(i)));
        socketSendMessage(message);
    }

    @Override
    public void notifyToolCards(List<ToolCard> toolCards){
        JsonObject message;
        JsonObject tmp;
        message = createMessage("toolcards");
        for (Integer i = 0; i < toolCards.size(); i++) {
            tmp = new JsonObject();
            tmp.addProperty("name", toolCards.get(i).getName());
            message.add(i.toString(), tmp);
        }
        socketSendMessage(message);
    }

    @Override
    public void notifyToolCardUse(String player, String toolcard, Window window){
        JsonObject message;
        message = createMessage("toolcard-used");
        message.addProperty("player", player);
        message.addProperty("toolcard", toolcard);
        message.addProperty("window", gson.toJson(window));
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
    public void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound){
        JsonObject message;
        message = createMessage("round");
        message.addProperty("player", currentPlayer);
        message.addProperty("draft-pool", gson.toJson(draftPool));
        message.addProperty("new-round", newRound);
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
    public Coordinate askDiceWindow() {
        JsonObject command;
        socketSendMessage(createMessage("toolcard-dice-window"));
        try{
            command = socketReadCommand();

            return gson.fromJson(command.get("choice").getAsString(), Coordinate.class);

        } catch (NullPointerException e){
            Logger.print("Disconnected: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
        }

        return null;
    }

    @Override
    public Dice askDiceDraftPool(){
        JsonObject command;
        socketSendMessage(createMessage("toolcard-dice-draftpool"));
        try{
            command = socketReadCommand();

            return gson.fromJson(command.get("choice").getAsString(), Dice.class);

        } catch (NullPointerException e){
            Logger.print("Disconnected: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
        }
        return null;
    }

    @Override
    public int askDiceRoundTrack(){
        JsonObject command;
        socketSendMessage(createMessage("toolcard-dice-roundtrack"));
        try{
            command = socketReadCommand();

            return getJsonPositiveIntValue(command, "choice");

        } catch (NullPointerException e){
            Logger.print("Disconnected: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
        }
        return -1;
    }

    @Override
    public boolean askIfPlus(){
        JsonObject command;
        socketSendMessage(createMessage("toolcard-plus-minus"));
        try{
            command = socketReadCommand();
            return command.get("choice").getAsBoolean();

        } catch (NullPointerException e){
            Logger.print("Disconnected: " + nickname + " " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
            throw new NullPointerException();
        }

    }

    private boolean login() {
        JsonObject command;
        String password;

        try {
            command = socketReadCommand();
            try{
                if (command.get("command").getAsString().equals("login")){
                    this.nickname = command.get(SocketHandler.NICKNAME_STRING).getAsString();
                    password = command.get("password").getAsString();
                    if (gameFlowHandler.login(this.nickname, password, this)){
                        socketSendMessage(createMessage("verified"));
                        return true;
                    }else{
                        socketSendMessage(createMessage("failed"));
                        return false;
                    }
                }
            }catch (NullPointerException e){
                this.nickname = null;
                socketSendMessage(createMessage("Invalid option"));
            }
        }catch (NullPointerException e){
            Logger.print("Disconnected before login: " + socket.getRemoteSocketAddress().toString());
            this.gameFlowHandler.disconnected();
            connected = false;
        }
        return false;

    }

    private void socketSendMessage(JsonObject json) {
        output.println(json);
        output.flush();
    }

    private void socketPrintLine(String json) {
        output.println(json);
        output.flush();
    }

    private JsonObject socketReadCommand(){
        try {
            return parser.parse(socketReadLine()).getAsJsonObject();
        }
        catch (IllegalStateException e){
        }
        return null;
    }

    private void socketPrint(String p) {
        output.print(p);
        output.flush();
    }

    private String socketReadLine(){
        try {
            return input.readLine();
        } catch(SocketException e){
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.print("Exception while reading.");
        }
        return null;
    }

    private void socketClose(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.print("Exception while closing connection.");
        }
    }

    public void close(){
        if (this.connected){
            this.connected = false;
            socketClose();
        }
    }

    public String getRemoteAddress(){
        return socket.getRemoteSocketAddress().toString();
    }

    @Override
    public void setGame(Game game){
        this.gameFlowHandler.setGame(game);
    }

    private static JsonObject createErrorMessage(String description){
        JsonObject message;
        message = createMessage("failed");
        message.addProperty("info", description);
        return message;
    }

    private static JsonObject createMessage(String message){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", message);
        return jsonObject;
    }
}
