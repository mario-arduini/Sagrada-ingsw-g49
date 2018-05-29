package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
                        server.resultLogin(true);
                        break;
                    case "failed":
                        server.resultLogin(false);
                        break;
                    case "privateGoal":
                        client.setPrivateGoal(null);  //TODO
                        break;
                    case "schema":
                        List<Schema> schemas = new ArrayList<>();
                        //listType = new TypeToken<List<Schema>>(){}.getType();
                        for(Integer i = 0; i < jsonObject.keySet().size() - 1; i++)
                            schemas.add(gson.fromJson(jsonObject.get(i.toString()).getAsString(), Schema.class));
                        client.print_schemas(schemas);
                        Schema choseSchema = schemas.get(client.chooseSchema());
                        client.getGameSnapshot().getPlayer().setWindow(choseSchema);
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
