package it.polimi.ingsw.network.server;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Schema;

import java.util.List;

public interface ConnectionHandler{
    void notifyLogin(String nickname);
    void notifyLogin(List<String> nicknames);
    void notifyLogout(String nickname);
    void notifySchemas(List<Schema> schemas);
    String getRemoteAddress();
    void close();
    void setGame(Game game);

}
