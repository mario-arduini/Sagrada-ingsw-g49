package it.polimi.ingsw.network.client;

public interface Connection {
    boolean login(String nickname, String password);
    boolean sendSchema(int schema);
    boolean placeDice(Dice dice, int row, int column);
    boolean useToolCard(String name);
    void pass();
    void logout();

    //region TOOLCARD

    void sendPlusMinusOption(String choice);
    void sendDiceFromDraftPool(Dice dice);
    void sendDiceFromRoundTrack(int index);
    void sendDiceFromWindow(Coordinate coordinate);
    void sendPlacementPosition(Coordinate coordinate);

    //endregion
}
