package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.network.client.model.*;

import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;

public class ServerListener implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName() );
    private Client client;
    private ClientSocketHandler server;
    private boolean connected;
    private static Gson gson = new Gson();

    ServerListener(Client client, ClientSocketHandler server) {
        this.client = client;
        this.server = server;
        connected = true;
        ClientLogger.initLogger(LOGGER);
    }

    @Override
    public void run() {

        JsonObject jsonObject;
        JsonParser parser = new JsonParser();
        Type listType;

        while (connected){
            jsonObject = new JsonObject();
            try {
                jsonObject = parser.parse(server.socketReadLine()).getAsJsonObject();  //TODO: bring here socketReadLine
            } catch (IllegalStateException e) {
                LOGGER.warning(e.toString());
                continue;
            } catch (NullPointerException e)
            {
                connected = false;
                client.serverDisconnected();     //server.close ??
            }

            try {
                switch (jsonObject.get("message").getAsString()) {
                    case "welcome":
                        client.welcomePlayer();
                        break;
                    case "new_player":
                        List<String> players = gson.fromJson(jsonObject.get("nicknames").getAsString(), new TypeToken<List<String>>(){}.getType());
                        client.notifyLogin(players);
                        break;
                    case "quit":
                        if(!client.isGameStarted()) {
                            client.notifyLogout(jsonObject.get("nickname").getAsString());
                        }
                        break;
                    case "verified":
                        client.setServerResult(true);
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
                    case "reconnect-info":
                        HashMap<String,Window> playersWindow = gson.fromJson(jsonObject.get("windows").getAsString(),new TypeToken<HashMap<String,Window>>(){}.getType());
                        HashMap<String,Integer> favorMap = gson.fromJson(jsonObject.get("favor-token").getAsString(),new TypeToken<HashMap<String,Integer>>(){}.getType());
                        client.notifyReconInfo(playersWindow, favorMap, gson.fromJson(jsonObject.get("round-track").getAsString(),new TypeToken<List<Dice>>(){}.getType()));
                        break;
                    case "game-over":
                        client.notifyEndGame(gson.fromJson(jsonObject.get("scores").getAsString(), new TypeToken<List<Score>>(){}.getType()));
                        break;

                    //region TOOLCARD
                    case "toolcard-plus-minus":
                        server.sendPlusMinusOption(client.askIfPlus(jsonObject.get("prompt").getAsString()));
                        break;
                    case "toolcard-dice-draftpool":
                        server.sendDiceFromDraftPool(client.askDiceDraftPool(jsonObject.get("prompt").getAsString()));
                        break;
                    case "toolcard-dice-roundtrack":
                        server.sendDiceFromRoundTrack(client.askDiceRoundTrack(jsonObject.get("prompt").getAsString()));
                        break;
                    case "toolcard-dice-window":
                        server.sendDiceFromWindow(client.askDiceWindow(jsonObject.get("prompt").getAsString()));
                        break;
                    case "toolcard-dice-value":
                        server.sendDiceValue(client.askDiceValue(jsonObject.get("prompt").getAsString()));
                        break;

                    //endregion

                    default:
                        break;
                }
            } catch (RemoteException | NullPointerException e) {
                LOGGER.warning(e.toString());
            }
        }
    }

    void setConnected(boolean connected){
        this.connected = connected;
    }
}
