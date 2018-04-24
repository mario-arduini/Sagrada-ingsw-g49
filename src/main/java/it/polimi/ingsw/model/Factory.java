package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.Set;

public class Factory {
    private ArrayList<Integer> toolCards;
    private ArrayList<Color> publicGoalCards;
    private ArrayList<Integer> privateGoalCards;
    private ArrayList<Dice> diceBag;
    private ArrayList<Integer> schemas;

    public Factory(){

    }

    private void shuffleCards(){
        // TODO
    }

    public ArrayList<Schema> extractSchemas(int schemasNumber){
        // TODO
        return null;
    }

    protected ToolCard extractToolCard(){
        // TODO
        return null;
    }

    protected PrivateGoal extractPrivateGoal(){
        // TODO
        return null;
    }

    protected PublicGoal extractPublicGoal(){
        // TODO
        return null;
    }

    protected Set<Dice> extractPool(int dicesNumber){
        // TODO
        return null;
    }

}
