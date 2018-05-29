package it.polimi.ingsw.network.client;

public interface Connection {
    boolean login();
    boolean sendSchema(int schema);
    boolean placeDice(Dice dice, int row, int column);
    void pass();
    void logout();
}
