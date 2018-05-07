package it.polimi.ingsw.model;

public class FullShadeVariety extends PublicGoal {
    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public int computeScore(Window window) {
        return 0;
    }
}
