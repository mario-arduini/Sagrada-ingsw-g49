package it.polimi.ingsw.model.goalcards;

import it.polimi.ingsw.model.Window;

public interface Goal {
    String getName();
    int computeScore(Window window);
}
