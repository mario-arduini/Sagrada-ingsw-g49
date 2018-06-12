package it.polimi.ingsw.model.goalcards;

public abstract class PublicGoal implements Goal{
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
