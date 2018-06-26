package it.polimi.ingsw.network.client;

import it.polimi.ingsw.network.client.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ClientInterface {
    void notifyLogin(String nickname);
    void notifyLogin(List<String> nicknames);
    void notifyLogout(String nickname);
    void notifySchemas(List<Schema> schemas);
    //TODO: as in gameRoom, maybe overload..??
    void notifyRound(String currentPlayer, List<Dice> draftPool, boolean newRound, List<Dice> roundTrack);
    void notifyOthersSchemas(Map<String, Schema> playersSchemas);
    void notifyDicePlaced(String nickname, int row, int column, Dice dice);
    void notifyToolCardUse(String player, String toolCard, Window window, List<Dice> draftPool, List<Dice> roundTrack);
    void notifyGameInfo(List<String> toolCards, List<String> publicGoals, String privateGoal);
    void notifyReconInfo(HashMap<String, Window> windows, HashMap<String, Integer> favorToken, List<Dice> roundTrack);
    void notifyEndGame(List<Score> scores);

    //void close();

    Coordinate askDiceWindow(String prompt);
    Dice askDiceDraftPool(String prompt);
    int askDiceRoundTrack(String prompt);
    boolean askIfPlus(String prompt);
    int askDiceValue(String prompt);
}
