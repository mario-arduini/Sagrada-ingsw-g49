package it.polimi.ingsw.network.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.network.client.model.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;

public class ServerListener implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName() );
    private Client client;
    private ClientSocketHandler server;
    private boolean connected;
    private GameSnapshot gameSnapshot;
    private static Gson gson = new Gson();

    ServerListener(Client client, ClientSocketHandler server, GameSnapshot gameSnapshot) {
        this.gameSnapshot = gameSnapshot;
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

                    //region DEPRECATED
                    case "waiting-room":
                        listType = new TypeToken<List<String>>(){}.getType();
                        client.updateWaitingRoom(gson.fromJson(jsonObject.get("nicknames").getAsString(), listType), jsonObject.get("new").getAsBoolean());
                        break;
                    //endregion

                    case "new_player":
                        client.updateWaitingRoom(gson.fromJson(jsonObject.get("nicknames").getAsString(), new TypeToken<List<String>>(){}.getType()), true);
                        break;
                    case "quit":
                        if(!client.isGameStarted()) {
                            List<String> loggedOutUser = new ArrayList<>();
                            loggedOutUser.add(jsonObject.get("nickname").getAsString());
                            client.updateWaitingRoom(loggedOutUser, false);
                        }
                        break;
                    case "verified":
                        server.notifyResult(true);
                        break;
                    case "failed":
                        server.notifyResult(false);
                        break;
                    case "game-info":
                        extractToolCards(jsonObject.getAsJsonObject("toolcards"));
                        extractPublicGoals(jsonObject.getAsJsonObject("public-goals"));
                        gameSnapshot.setPrivateGoal(jsonObject.get("private-goal").getAsString()); //TODO: change this to only string?
                        break;
                    case "schema-choice":
                        List<Schema> schemas = new ArrayList<>();
                        for(Integer i = 0; i < jsonObject.keySet().size() - 1; i++)
                            schemas.add(gson.fromJson(jsonObject.get(i.toString()).getAsString(), Schema.class));
                        client.chooseSchema(schemas);
                        break;
                    case "round":
                        listType = new TypeToken<List<Dice>>(){}.getType();
                        client.notifyNewTurn(jsonObject.get("player").getAsString());
                        if(jsonObject.get("new-round").getAsBoolean())
                            client.getGameSnapshot().setRoundTrack(gson.fromJson(jsonObject.get("round-track").getAsString(),listType));
                        List<Dice> draftPool = gson.fromJson(jsonObject.get("draft-pool").getAsString(), listType);
                        client.getGameSnapshot().setDraftPool(draftPool);
                        client.printGame();
                        client.printMenu();
                        break;
                    case "schema-chosen":
                        HashMap<String, Schema> windows = gson.fromJson(jsonObject.get("content").getAsString(), new TypeToken<HashMap<String, Schema>>(){}.getType());
                        for (Map.Entry<String, Schema> entry : windows.entrySet()) {
                            if(!entry.getKey().equals(client.getGameSnapshot().getPlayer().getNickname()))
                                client.getGameSnapshot().findPlayer(entry.getKey()).get().setWindow(entry.getValue());
                            else
                                client.getGameSnapshot().getPlayer().setWindow(entry.getValue());
                        }
                        break;
                    case "toolcard-used":
                        ToolCard toolcardUsed = client.getGameSnapshot().getToolCardByName(jsonObject.get("toolcard").getAsString());
                        PlayerSnapshot activePlayer = client.getGameSnapshot().findPlayer(jsonObject.get("player").getAsString()).get();
                        activePlayer.useFavorToken(toolcardUsed.getUsed() ? 2 : 1);
                        toolcardUsed.setUsed();
                        activePlayer.setWindow(gson.fromJson(jsonObject.get("window").getAsString(),Window.class));
                        client.getGameSnapshot().setRoundTrack(gson.fromJson(jsonObject.get("round-track").getAsString(),new TypeToken<List<Dice>>(){}.getType()));
                        client.getGameSnapshot().setDraftPool(gson.fromJson(jsonObject.get("draft-pool").getAsString(),new TypeToken<List<Dice>>(){}.getType()));
                        client.getGameSnapshot().getPlayer().setUsedToolCard(activePlayer.getNickname().equalsIgnoreCase(client.getGameSnapshot().getPlayer().getNickname()));
                        client.printGame();
                        client.printMenu();
                        break;
                    case "update-window":
                        Dice dicePlaced = gson.fromJson(jsonObject.get("dice").getAsString(),Dice.class);
                        int row = jsonObject.get("row").getAsInt();
                        int col = jsonObject.get("column").getAsInt();
                        String nick = jsonObject.get("nickname").getAsString();
                        client.getGameSnapshot().getDraftPool().remove(dicePlaced);
                        if(!nick.equals(client.getGameSnapshot().getPlayer().getNickname()))
                            client.getGameSnapshot().findPlayer(nick).get().getWindow().addDice(row,col,dicePlaced);
                        else {
                            client.getGameSnapshot().getPlayer().getWindow().addDice(row, col, dicePlaced);
                            server.notifyResult(true);
                        }
                        client.printGame();
                        client.printMenu();
                        break;
                    case "reconnect-info":
                        HashMap<String,Window> playersWindow = gson.fromJson(jsonObject.get("windows").getAsString(),new TypeToken<HashMap<String,Window>>(){}.getType());
                        HashMap<String,Integer> favorMap = gson.fromJson(jsonObject.get("favor-token").getAsString(),new TypeToken<HashMap<String,Integer>>(){}.getType());
                        client.getGameSnapshot().setRoundTrack(gson.fromJson(jsonObject.get("round-track").getAsString(),new TypeToken<List<Dice>>(){}.getType()));

                        PlayerSnapshot playerSnapshot;
                        for(String user : playersWindow.keySet()){
                            playerSnapshot = new PlayerSnapshot(user);
                            playerSnapshot.setWindow(playersWindow.get(user));
                            playerSnapshot.setFavorToken(favorMap.get(user));
                            if(user.equals(client.getGameSnapshot().getPlayer().getNickname()))
                                client.getGameSnapshot().setPlayer(playerSnapshot);
                            else
                                client.getGameSnapshot().addOtherPlayer(playerSnapshot);
                        }
                        break;
                    case "game-over":
                        client.gameOver(gson.fromJson(jsonObject.get("scores").getAsString(), new TypeToken<List<Score>>(){}.getType()));
                        break;

                    //region TOOLCARD
                    case "toolcard-plus-minus":
                        server.sendPlusMinusOption(client.getPlusMinusOption(jsonObject.get("prompt").getAsString()));
                        break;
                    case "toolcard-dice-draftpool":
                        server.sendDiceFromDraftPool(client.getDiceFromDraftPool(jsonObject.get("prompt").getAsString()));
                        break;
                    case "toolcard-dice-roundtrack":
                        server.sendDiceFromRoundTrack(client.getDiceFromRoundTrack(jsonObject.get("prompt").getAsString()));
                        break;
                    case "toolcard-dice-window":
                        server.sendDiceFromWindow(client.getDiceFromWindow(jsonObject.get("prompt").getAsString()));
                        break;
                    case "toolcard-place-window":
                        server.sendPlacementPosition(client.getPlacementPosition());
                        break;
                    case "toolcard-dice-value":
                        server.sendDiceValue(client.getDiceValue(jsonObject.get("prompt").getAsString()));
                        break;

                    //endregion

                    default:
                        break;
                }
            } catch (NullPointerException e) {
                LOGGER.warning(e.toString());
            }
        }
    }

    private void extractToolCards(JsonObject jsonObject){
        List<ToolCard> toolCards = new ArrayList<>();
        for(Integer i = 0; i < 3; i++)
            toolCards.add(new ToolCard(jsonObject.get(i.toString()).getAsJsonObject().get("name").getAsString(),"")); //jsonObject.get("0").getAsJsonObject().get("description").getAsString()));
        gameSnapshot.setToolCards(toolCards);
    }

    private void extractPublicGoals(JsonObject jsonObject){
        List<String> publicGoals = new ArrayList<>();
        for(Integer i = 0; i < 3; i++)
            publicGoals.add(jsonObject.get(i.toString()).getAsJsonObject().get("name").getAsString());
        gameSnapshot.setPublicGoals(publicGoals);
    }

    void setConnected(boolean connected){
        this.connected = connected;
    }
}
