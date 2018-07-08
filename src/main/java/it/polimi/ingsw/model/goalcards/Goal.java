package it.polimi.ingsw.model.goalcards;

import it.polimi.ingsw.model.Window;

/**
 * Interface representing a Goal in the Game
 */
public interface Goal {
    /**
     * Get the name of the Goal
     * @return name of the Goal
     */
    String getName();

    /**
     * Compute the Score of the given Window
     * @param window Window to use for the computation
     * @return score value of the given window
     */
    int computeScore(Window window);
}
