package it.polimi.ingsw.network.client;

public interface Connection {
    boolean login();
    boolean sendSchema(int schema);
    boolean placeDice(int dice, int row, int column);
    void logout();
}
