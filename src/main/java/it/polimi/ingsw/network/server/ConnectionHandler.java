package it.polimi.ingsw.network.server;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Schema;

import java.util.HashMap;
import java.util.List;

public interface ConnectionHandler{
    void notifyLogin(String nickname);
    void notifyLogin(List<String> nicknames);
    void notifyLogout(String nickname);
    void notifySchemas(List<Schema> schemas);
    void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound);
    void notifyOthersSchemas(HashMap<String, Schema> playersSchemas);
    String getRemoteAddress();
    void close();
    void setGame(Game game);

}
