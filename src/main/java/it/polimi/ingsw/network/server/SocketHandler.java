package it.polimi.ingsw.network.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.polimi.ingsw.controller.GameFlowHandler;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Schema;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class SocketHandler implements Runnable, ConnectionHandler{
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String nickname;
    private boolean connected;
    private GameFlowHandler gameFlowHandler;
    private Gson gson;
    JsonObject jsonObject;
    JsonParser parser;



    public SocketHandler(Socket socket, GameFlowHandler gameFlowHandler) {
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
                            gameFlowHandler.logout();
                            connected = false;
                            socketClose();
                            break;
                        //debugging    
                        case "players":
                            List<String> players = gameFlowHandler.getPlayers();

                            socketPrintLine(players.toString());
                            break;
                        case "schema":
                            try {
                                gameFlowHandler.chooseSchema(getJsonPositiveIntValue(message, "id"));
                                socketSendMessage(createMessage("verified"));
                            } catch (IndexOutOfBoundsException e){
                                socketSendMessage(createMessage("failed"));
                            }
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

    private String getJsonStringValue(JsonObject message, String key){
        try {
            return message.get(key).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    private int getJsonPositiveIntValue(JsonObject message, String key){
        try {
            return message.get(key).getAsInt();
        }catch (Exception e){
            return -1;
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
        message.addProperty("nickname", nickname);
        socketSendMessage(message);
    }

    @Override
    public void notifySchemas(List<Schema> schemas){
        JsonObject message;
        message = createMessage("schema");
        for (Integer i = 0; i < schemas.size(); i++)
            message.addProperty(i.toString(), gson.toJson(schemas.get(i)));
        socketSendMessage(message);
    }

    private JsonObject createMessage(String message){
        jsonObject = new JsonObject();
        jsonObject.addProperty("message", message);
        return jsonObject;
    }

    private boolean login() {
        JsonObject command;
        String password;

        try {
            command = socketReadCommand();
            try{
                if (command.get("command").getAsString().equals("login")){
                    this.nickname = command.get("nickname").getAsString();
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

}