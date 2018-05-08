package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private final Factory dealer;
    private final ArrayList<Player> players;
    private Dice[] roundTrack;
    private int trackIndex;
    private final ToolCard[] toolCards;
    private final PublicGoal[] publicGoals;
    private int nextFirstPlayer;
    private Round currentRound;

    public Game(ArrayList<Player> players) { //Fix UML for players
        this.dealer = new Factory();
        this.roundTrack = new Dice[10];
        this.toolCards = new ToolCard[3];
        this.trackIndex = 0;
        this.publicGoals = new PublicGoal[3];
        this.players = new ArrayList<Player>();
        for (int i = 0; i < players.size(); i++)
            this.players.add(players.get(i));
        for (int j = 0; j<3; j++)
            this.publicGoals[j] = dealer.extractPublicGoal();
        nextFirstPlayer = (new Random()).nextInt(players.size());
        nextRound();
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

    public Dice[] getRoundTrack() {
        return roundTrack;
    }

    public List<Dice> getDiceFromPool(){
        return currentRound.getDraftPool();
    }

    private void addDiceToTracker(Dice dice){
        this.roundTrack[trackIndex++] = new Dice(dice);
    }

    public Player getPlayerByNick(String nick){
        return (Player) players.stream().filter(player -> player.getNickname().equals(nick));
    }

    //Fix UML
    public Round getCurrentRound(){
        return new Round(currentRound);
    }

    public void placeDice(int row, int column, Dice dice) throws NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException {
        currentRound.useDice(row, column, dice);
    }

    //Throws Exception
    public void tryToolCard(){
        //TODO
    }

    public void suspendPlayer(){
        currentRound.suspendPlayer();
    }

    public int computeFinalScore(Player player){
        //TODO
        return 0;
    }

    public boolean isGameFinished(){
        return trackIndex == 11;
    }

    public void nextRound(){
        List<Player> roundPlayers = new ArrayList<Player>();
        List<Dice> draftPool;
        int size = players.size();
        int j;

        nextFirstPlayer = (nextFirstPlayer + 1)%size;
        //Add players
        for (j = nextFirstPlayer; j == (nextFirstPlayer - 1)%size; j = (j+1)%size)
            roundPlayers.add(players.get(j));
        roundPlayers.add(players.get(nextFirstPlayer - 1));

        //Add players in reverse order
        for (j = (nextFirstPlayer-1)%size; j == nextFirstPlayer; j = (j-1)%size)
            roundPlayers.add(players.get(j));
        roundPlayers.add(players.get(nextFirstPlayer));

        draftPool = currentRound.getDraftPool();
        addDiceToTracker(draftPool.get((new Random()).nextInt(draftPool.size())));
        currentRound = new Round(dealer.extractPool(2*(size) + 1), roundPlayers);
    }

}