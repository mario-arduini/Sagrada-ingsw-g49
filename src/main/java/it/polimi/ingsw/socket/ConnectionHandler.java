package it.polimi.ingsw.socket;

import java.util.List;

public interface ConnectionHandler {
    void notifyPlayers(String nickname);
    void notifyPlayers(List<String> nicknames);

}
