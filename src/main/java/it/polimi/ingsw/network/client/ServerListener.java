package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Score;
import it.polimi.ingsw.model.Window;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.*;
import java.util.logging.Logger;

public class ServerListener implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName() );
    private Client client;
    private ClientSocketHandler server;
    private BufferedReader input;
    private boolean connected;
    private static Gson gson = new Gson();

    ServerListener(Client client, ClientSocketHandler server, Socket socket) throws IOException {
        this.client = client;
        this.server = server;
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        connected = true;
        ClientLogger.initLogger(LOGGER);
    }

    @Override
    public void run() {

        JsonObject jsonObject;
        JsonParser parser = new JsonParser();
        Type listType;

        while (connected){
            try {
                jsonObject = parser.parse(socketReadLine()).getAsJsonObject();
            } catch (IllegalStateException e) {
                LOGGER.warning(e.toString());
                continue;
            } catch (NullPointerException e) {
                connected = false;
                try {
                    client.serverDisconnected();
                } catch (ServerReconnectedException e1) {
                    LOGGER.info(e.toString());
                }
                continue;
            }

            try {
                switch (jsonObject.get("message").getAsString()) {
                    case "new-player":
                        client.notifyLogin(jsonObject.get("nickname").getAsString());
                        break;
                    case "game-room":
                        List<String> players = gson.fromJson(jsonObject.get("players").getAsString(), new TypeToken<List<String>>(){}.getType());
                        client.notifyLogin(players);
                        break;
                    case "quit":
                        client.notifyLogout(jsonObject.get("nickname").getAsString());
                        break;
                    case "failed":
                        client.setServerResult(false);
                        break;
                    case "game-info":
                        listType = new TypeToken<List<String>>(){}.getType();
                        client.notifyGameInfo(gson.fromJson(jsonObject.get("toolcards").getAsString(), listType), gson.fromJson(jsonObject.get("public-goals").getAsString(), listType), jsonObject.get("private-goal").getAsString());
                        break;
                    case "schema-choice":
                        client.notifySchemas(gson.fromJson(jsonObject.get("schemas").getAsString(), new TypeToken<List<Schema>>(){}.getType()));
                        break;
                    case "round":
                        listType = new TypeToken<List<Dice>>(){}.getType();
                        if(jsonObject.get("new-round").getAsBoolean())
                            client.notifyRound(jsonObject.get("player").getAsString(), gson.fromJson(jsonObject.get("draft-pool").getAsString(), listType), true, gson.fromJson(jsonObject.get("round-track").getAsString(),listType));
                        else
                            client.notifyRound(jsonObject.get("player").getAsString(), gson.fromJson(jsonObject.get("draft-pool").getAsString(), listType), false, null);
                        break;
                    case "schema-chosen":
                        client.notifyOthersSchemas(gson.fromJson(jsonObject.get("content").getAsString(), new TypeToken<HashMap<String, Schema>>(){}.getType()));
                        break;
                    case "toolcard-used":
                        listType = new TypeToken<List<Dice>>(){}.getType();
                        client.notifyToolCardUse(jsonObject.get("player").getAsString(), jsonObject.get("toolcard").getAsString(), gson.fromJson(jsonObject.get("window").getAsString(),Window.class), gson.fromJson(jsonObject.get("draft-pool").getAsString(), listType), gson.fromJson(jsonObject.get("round-track").getAsString(), listType));
                        break;
                    case "update-window":
                        client.notifyDicePlaced(jsonObject.get("nickname").getAsString(), jsonObject.get("row").getAsInt(), jsonObject.get("column").getAsInt(), gson.fromJson(jsonObject.get("dice").getAsString(),Dice.class));
                        break;
                    case "suspended":
                        client.notifySuspension(jsonObject.get("player").getAsString());
                        break;
                    case "reconnect-info":
                        HashMap<String,Window> playersWindow = gson.fromJson(jsonObject.get("windows").getAsString(),new TypeToken<HashMap<String,Window>>(){}.getType());
                        HashMap<String,Integer> favorMap = gson.fromJson(jsonObject.get("favor-token").getAsString(),new TypeToken<HashMap<String,Integer>>(){}.getType());
                        client.notifyReconInfo(playersWindow, favorMap, gson.fromJson(jsonObject.get("round-track").getAsString(),new TypeToken<List<Dice>>(){}.getType()), jsonObject.get("toolcard").getAsString());
                        break;
                    case "game-over":
                        client.notifyEndGame(gson.fromJson(jsonObject.get("scores").getAsString(), new TypeToken<List<Score>>(){}.getType()));
                        break;

                    //region TOOLCARD
                    case "toolcard-plus-minus":
                        server.sendPlusMinusOption(client.askIfPlus(jsonObject.get("prompt").getAsString(), jsonObject.get("rollback").getAsBoolean()));
                        break;
                    case "toolcard-dice-draftpool":
                        server.sendDiceFromDraftPool(client.askDiceDraftPool(jsonObject.get("prompt").getAsString(), jsonObject.get("rollback").getAsBoolean()));
                        break;
                    case "toolcard-dice-roundtrack":
                        server.sendDiceFromRoundTrack(client.askDiceRoundTrack(jsonObject.get("prompt").getAsString(), jsonObject.get("rollback").getAsBoolean()));
                        break;
                    case "toolcard-dice-window":
                        server.sendDiceFromWindow(client.askDiceWindow(jsonObject.get("prompt").getAsString(), jsonObject.get("rollback").getAsBoolean()));
                        break;
                    case "toolcard-dice-value":
                        server.sendDiceValue(client.askDiceValue(jsonObject.get("prompt").getAsString(), jsonObject.get("rollback").getAsBoolean()));
                        break;
                    case "move-dice-number":
                        server.sendMoveNumber(client.askMoveNumber(jsonObject.get("prompt").getAsString(), jsonObject.get("number").getAsInt(), jsonObject.get("rollback").getAsBoolean()));
                        break;
                    case "show-dice":
                        client.showDice(gson.fromJson(jsonObject.get("dice").getAsString(), Dice.class));
                        break;
                    case "alert-dice":
                        client.alertDiceInDraftPool(gson.fromJson(jsonObject.get("dice").getAsString(), Dice.class));
                        break;
                    case "rollback-ok":
                        client.setServerResult(true);
                        break;
                    //endregion

                    default:
                        break;
                }
            } catch (NullPointerException e) {
                LOGGER.warning(e.toString());
            } catch (RollbackException e){
                server.sendRollback();
            }
        }
    }

    private String socketReadLine(){
        try {
            return input.readLine();
        } catch(Exception e) {
            throw new NullPointerException();
        }
    }
}
