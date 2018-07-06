package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.Coordinate;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Schema;
import it.polimi.ingsw.model.Score;
import it.polimi.ingsw.network.client.model.GameSnapshot;

import java.util.List;

public interface GraphicInterface {
    void printWaitingRoom();
    void printSchemaChoice(GameSnapshot gameSnapshot, List<Schema> schemas);
    void printGame(GameSnapshot gameSnapshot);
    void printMenu(GameSnapshot gameSnapshot);
    void notifyUsedToolCard(String player,String toolCard);
    void gameOver(List<Score> scores);
    void notifyServerDisconnected();
    boolean askIfPlus(String prompt, boolean rollback) throws RollbackException;
    Dice askDiceDraftPool(String prompt, boolean rollback) throws RollbackException;
    int askDiceRoundTrack(String prompt, boolean rollback) throws RollbackException;
    Coordinate askDiceWindow(String prompt, boolean rollback) throws RollbackException;
    int askDiceValue(String prompt, boolean rollback) throws RollbackException;
    int askMoveNumber(String prompt, int n, boolean rollback) throws RollbackException;
    void wakeUp(boolean serverResult);
    void interruptInput();
    void printDice(Dice dice);
    void setToolCardNotCompleted(String toolCard);
}
