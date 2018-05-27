package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ServerListener extends Thread {

    private Client client;
    private ClientSocketHandler server;
    private boolean connected;

    ServerListener(Client client, ClientSocketHandler server){
        this.client = client;
        this.server = server;
        connected = true;
    }

    @Override
    public void run() {

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        JsonParser parser = new JsonParser();
        String command;

        try {
            while (connected) {
                command = server.socketReadLine();
                //jsonObject = parser.parse(server.socketReadLine()).getAsJsonObject();
                //if (command != null) {
                switch (command.split(" ")[0]) {//jsonObject.get("command").getAsString()
                    case "welcome":
                        client.welcomePlayer();
                        break;
                    case "new_player":
                        //client.addPlayers(gson.fromJson(jsonObject.get("nicknames").getAsString(), String[].class));
                        client.addPlayers(command.substring(command.indexOf(' ') + 1).split(" "));
                        break;
                    case "quit":
                        //client.removePlayer(jsonObject.get("nickname").getAsString());
                        client.removePlayer(command.substring(command.indexOf(' ') + 1));
                        break;
                    case "login":
                        if(command.split(" ")[2].equals("token"))//jsonObject.get("token").getAsString().equals("")
                            server.sendToken();
                        else {
                            client.printToken(command.split(" ")[2]);//jsonObject.get("token").getAsString()
                            server.resultLogin(true);
                        }
                        break;
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
//                    }
//                } else {
//                    connected = false;
//                    client.serverDisconnected();     //server.close ??
//                }
                }
            }
        }catch(NullPointerException e){ // inside while? Remove connected?
            connected = false;
            client.serverDisconnected();     //server.close ??
        }
    }
    void setConnected(boolean connected){
        this.connected = connected;
    }
}
