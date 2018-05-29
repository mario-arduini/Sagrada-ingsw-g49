package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class ServerListener extends Thread {

    private Client client;
    private ClientSocketHandler server;
    private boolean connected;

    ServerListener(Client client, ClientSocketHandler server) {
        this.client = client;
        this.server = server;
        connected = true;
    }

    @Override
    public void run() {

        Gson gson = new Gson();
        JsonObject jsonObject;
        JsonParser parser = new JsonParser();
        Type listType;

        while (connected){
            jsonObject = null;
            try {
                jsonObject = parser.parse(server.socketReadLine()).getAsJsonObject();
            } catch (IllegalStateException e) {
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
                        listType = new TypeToken<List<String>>(){}.getType();
                        client.addPlayers(gson.fromJson(jsonObject.get("nicknames").getAsString(), listType));
                        break;
                    case "quit":
                        client.removePlayer(jsonObject.get("nickname").getAsString());
                        break;
                    case "verified":
                        client.initGameSnapshot();
                        server.notifyContinue(true);
                        break;
                    case "failed":
                        server.notifyContinue(false);
                        break;
                    case "start_game":
                        client.notifyStartGame();
                        break;
                    case "privateGoal":
                        client.setPrivateGoal(null);  //TODO
                        break;
                    case "schema-choice":
                        List<Schema> schemas = new ArrayList<>();
                        for(Integer i = 0; i < jsonObject.keySet().size() - 1; i++)
                            schemas.add(gson.fromJson(jsonObject.get(i.toString()).getAsString(), Schema.class));
                        client.printSchemas(schemas);
                        Schema choseSchema = schemas.get(client.chooseSchema());
                        client.getGameSnapshot().getPlayer().setWindow(choseSchema);
                        break;
                    case "round":
                        listType = new TypeToken<List<Dice>>(){}.getType();
                        client.notifyNewRound(jsonObject.get("player").getAsString(), jsonObject.get("new-round").getAsBoolean());
                        List<Dice> draftPool = gson.fromJson(jsonObject.get("draft-pool").getAsString(), listType);
                        client.getGameSnapshot().setDraftPool(draftPool);
                        client.printGame();
                        client.playRound(draftPool);
                        break;
                    case "schema-chosen":
                        listType = new TypeToken<HashMap<String, Schema>>(){}.getType();
                        HashMap<String, Schema> windows = gson.fromJson(jsonObject.get("content").getAsString(), listType);
                        for (Map.Entry<String, Schema> entry : windows.entrySet()) {
                            if(!entry.getKey().equals(client.getGameSnapshot().getPlayer().getNickname())) client.getGameSnapshot().addOtherPlayer(entry.getKey(),entry.getValue());
                        }
//                        Set<String> keys = innerObject.keySet();
//                        keys.remove("schema-chosen");
//                        for(String key : keys)
//                            windows.put(key, gson.fromJson(innerObject.get(key).getAsString(), Schema.class));
                        //client.printOthersSchema(windows);
                        break;
                    default:
                        break;
                }
            } catch (NullPointerException e) {
            }
        }
    }
    void setConnected(boolean connected){
        this.connected = connected;
    }
}
