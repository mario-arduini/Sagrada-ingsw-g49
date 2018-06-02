package it.polimi.ingsw.network.server;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Schema;

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
    String getRemoteAddress();
    void close();
    void setGame(Game game);

}
