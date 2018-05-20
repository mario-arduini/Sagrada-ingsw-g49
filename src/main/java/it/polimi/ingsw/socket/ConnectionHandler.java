package it.polimi.ingsw.socket;

import java.util.List;

public interface ConnectionHandler {
    void notifyLogin(String nickname);
    void notifyLogin(List<String> nicknames);
    void notifyLogout(String nickname);

}
