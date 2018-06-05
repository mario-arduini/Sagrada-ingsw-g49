package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public class ServerListener implements Runnable {

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
                jsonObject = parser.parse(server.socketReadLine()).getAsJsonObject();  //TODO: bring here socketReadLine
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
                        server.notifyResult(true);
                        break;
                    case "failed":
                        server.notifyResult(false);
                        break;
                    case "privateGoal":
                        client.setPrivateGoal(null);  //TODO
                        break;
                    case "publicGoal":  //TODO
                        break;
                    case "toolCard":
                        List<ToolCard> toolCards = new ArrayList<>();
                        toolCards.add(new ToolCard(jsonObject.get("0").getAsJsonObject().get("name").getAsString(),jsonObject.get("0").getAsJsonObject().get("description").getAsString()));
                        toolCards.add(new ToolCard(jsonObject.get("1").getAsJsonObject().get("name").getAsString(),jsonObject.get("1").getAsJsonObject().get("description").getAsString()));
                        toolCards.add(new ToolCard(jsonObject.get("2").getAsJsonObject().get("name").getAsString(),jsonObject.get("2").getAsJsonObject().get("description").getAsString()));
                        client.getGameSnapshot().setToolCards(toolCards);
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
                        client.notifyNewTurn(jsonObject.get("player").getAsString(), jsonObject.get("new-round").getAsBoolean());
                        if(jsonObject.get("new-round").getAsBoolean())
                            client.getGameSnapshot().getRoundTrack().add(gson.fromJson(jsonObject.get("roundtrack-dice").getAsString(),Dice.class));
                        List<Dice> draftPool = gson.fromJson(jsonObject.get("draft-pool").getAsString(), listType);
                        client.getGameSnapshot().setDraftPool(draftPool);
                        client.printGame();
                        break;
                    case "schema-chosen":
                        listType = new TypeToken<HashMap<String, Schema>>(){}.getType();
                        HashMap<String, Schema> windows = gson.fromJson(jsonObject.get("content").getAsString(), listType);
                        for (Map.Entry<String, Schema> entry : windows.entrySet()) {
                            if(!entry.getKey().equals(client.getGameSnapshot().getPlayer().getNickname()))
                                client.getGameSnapshot().addOtherPlayer(entry.getKey(), entry.getValue());
                        }
                        break;
                    case "toolCard-used":
                        List<ToolCard> toolCards1 = client.getGameSnapshot().getToolCards();
                        String name = jsonObject.get("name").getAsString();
                        for (ToolCard aToolCards1 : toolCards1)
                            if (aToolCards1.getName().equalsIgnoreCase(name))
                                aToolCards1.setUsed();
                        break;
                    case "update-window":
                        Dice dicePlaced = gson.fromJson(jsonObject.get("dice").getAsString(),Dice.class);
                        int row = jsonObject.get("row").getAsInt();
                        int col = jsonObject.get("column").getAsInt();
                        String nick = jsonObject.get("nickname").getAsString();
                        if(!nick.equals(client.getGameSnapshot().getPlayer().getNickname())){
                            client.getGameSnapshot().getDraftPool().remove(dicePlaced);
                            client.getGameSnapshot().findPlayer(nick).get().getWindow().addDice(row,col,dicePlaced);
                            client.printGame();
                        }
                        break;

                    //region TOOLCARD

                    case "toolcard-plus-minus":
                        client.getPlusMinusOption();
                        break;
                    case "toolcard-dice-draftpool":
                        client.getDiceFromDraftPool();
                        break;
                    case "toolcard-dice-roundtrack":
                        client.getDiceFromRoundTrack();
                        break;
                    case "toolcard-dice-window":
                        client.getDiceFromWindow();
                        break;
                    case "toolcard-place-window":
                        client.getPlacementPosition();
                        break;

                    //endregion

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
