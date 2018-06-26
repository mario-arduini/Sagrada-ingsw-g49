package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.model.Coordinate;
import it.polimi.ingsw.network.client.model.Dice;

public interface Connection {
    void login(String nickname, String password);
    void sendSchema(int schema);
    void placeDice(Dice dice, int row, int column);
    void useToolCard(String name);
    void pass();
    void logout();
}
