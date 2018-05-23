package it.polimi.ingsw.network.server;

import java.util.List;

public interface ConnectionHandler{
    void notifyLogin(String nickname);
    void notifyLogin(List<String> nicknames);
    void notifyLogout(String nickname);
    String getRemoteAddress();
    void close();
}
