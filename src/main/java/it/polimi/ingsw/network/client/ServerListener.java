package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

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
        JsonObject jsonObject = null;
        JsonParser parser = new JsonParser();

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
                        client.addPlayers(gson.fromJson(jsonObject.get("nicknames").getAsString(), String[].class));
                        break;
                    case "quit":
                        client.removePlayer(jsonObject.get("nickname").getAsString());
                        break;
    //                        case "login":
    //                            if (jsonObject.get("token").getAsString().equals(""))
    //                                server.sendToken();
    //                            else {
    //                                client.printToken(jsonObject.get("token").getAsString());
    //                                server.resultLogin(true);
    //                            }
    //                            break;
                    case "verified":
                        server.resultLogin(true);
                        break;
                    case "failed":
                        server.resultLogin(false);
                        break;
                    case "privateGoal":
                        client.setPrivateGoal(null);
                        break;
                    case "schema":

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
