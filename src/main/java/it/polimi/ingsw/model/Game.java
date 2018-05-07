package it.polimi.ingsw.model;

import java.util.ArrayList;

public class Game {
    private final Factory dealer;
    private final ArrayList<Player> players;
    private Dice[] roundTrack;
    private final ToolCard[] toolCards;
    private final PublicGoal[] publicGoals;
    private int nextFirstPlayer;
    private Round currentRound;

    public Game(ArrayList<Player> players) { //Fix UML for players
        this.dealer = new Factory();
        this.roundTrack = new Dice[10];
        this.toolCards = new ToolCard[3];
        this.publicGoals = new PublicGoal[3];
        this.players = new ArrayList<Player>();
        for (int i = 0; i < players.size(); i++)
            this.players.add(players.get(i));
        for (int j = 0; j<3; j++)
            this.publicGoals[j] = dealer.extractPublicGoal();
        this.currentRound = new Round(dealer.extractPool(2*(players.size()) + 1), players);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ToolCard[] getToolCards() {
        return toolCards;
    }

    public PublicGoal[] getPublicGoals() {
        return publicGoals;
    }

    //Throws Exception
    public Dice getDiceFromPool(){
        //TODO
        return  null;
    }

    public void setDraftPool(){
        //TODO
    }

    //Throws Exception
    public void addDiceToTracker(Dice dice){
        //TODO
    }

    public Player getPlayerByName(String name){
        //TODO
        return null;
    }

    //Fix UML
    public Round getCurrentRound(){
        //TODO
        return null;
    }

    //Throws Exception
    public void placeDice(int row, int column, Dice dice){
        //TODO
    }


    //Throws Exception
    public void tryToolCard(){
        //TODO
    }

    public void suspendPlayer(){
        //TODO
    }

    public int computeFinalScore(Player player){
        //TODO
        return 0;
    }

    public void nextRound(){
        //TODO
    }

}