package it.polimi.ingsw.model.client;

public interface Connection {
    boolean login(String nickname);
    void logout();
}
