package it.polimi.ingsw.network.client;

public interface Connection {
    boolean login();
    void sendSchema(int schema);
    void logout();
}
