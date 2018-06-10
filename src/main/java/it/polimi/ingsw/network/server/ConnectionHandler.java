package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameRoom;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.toolcards.ToolCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ConnectionHandler{
    void notifyLogin(String nickname);
    void notifyLogin(List<String> nicknames);
    void notifyLogout(String nickname);
    void notifySchemas(List<Schema> schemas);
    void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound);
    void notifyOthersSchemas(Map<String, Schema> playersSchemas);
    void notifyDicePlaced(String nickname, int row, int column, Dice dice);
    void notifyToolCardUse(String player, String toolCard, Window window);
    void notifyToolCards(List<ToolCard> toolCards);
    String getRemoteAddress();
    void close();
    void setGame(GameRoom game);

    Coordinate askDiceWindow();
    Dice askDiceDraftPool();
    int askDiceRoundTrack();
    boolean askIfPlus();
    int askDiceValue();
}
