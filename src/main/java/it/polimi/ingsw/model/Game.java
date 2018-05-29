package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.model.toolcards.ToolCard;

import java.util.*;

public class Game {
    private final Factory dealer;
    private final List<Player> players;
    private Dice[] roundTrack;
    private int trackIndex;
    private final ToolCard[] toolCards;
    private final PublicGoal[] publicGoals;
    private int nextFirstPlayer;
    private Round currentRound;
    private static final int schemaPerPlayer = 4;
    private boolean playing;

    public Game(List<Player> playerList) throws NoMorePlayersException { //Fix UML for players
        this.dealer = new Factory();
        this.roundTrack = new Dice[10];
        this.toolCards = new ToolCard[3];
        this.trackIndex = 0;
        this.publicGoals = new PublicGoal[3];
        this.players = new ArrayList<>();
        this.players.addAll(playerList);
        for (int j = 0; j<3; j++) {
            try {
                this.publicGoals[j] = dealer.extractPublicGoal();
            } catch (OutOfCardsException e) {
                e.printStackTrace();
            }
        }
        nextFirstPlayer = (new Random()).nextInt(players.size());

        int size = players.size();
        currentRound = new Round(dealer.extractPool(2*(size) + 1),createRoundPlayers(size));
        currentRound.nextPlayer();
        this.playing = false;
    }

    public void setPlaying(boolean playing){
        this.playing = playing;
    }

    public boolean getPlaying(){
        return playing;
    }

    public List<Player> getPlayers() {
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
        Optional<Player> playerFetched = players.stream().filter(player -> player.getNickname().equals(nick)).findFirst();

        if (!playerFetched.isPresent()) throw new NoSuchElementException();
        return playerFetched.get();
    }

    //TODO: Fix UML
    public Round getCurrentRound(){
        return new Round(currentRound);
    }

    public void placeDice(int row, int column, Dice dice) throws NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException {
        currentRound.useDice(row, column, dice);
    }

    //TODO: Throws Exception
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
        List<Player> roundPlayers;
        List<Dice> draftPool;

        int size = players.size();
        roundPlayers = createRoundPlayers(size);

        draftPool = currentRound.getDraftPool();
        addDiceToTracker(draftPool.get((new Random()).nextInt(draftPool.size())));
        currentRound = new Round(dealer.extractPool(2*(size) + 1), roundPlayers);
    }

    private List<Player> createRoundPlayers(int size){
        List<Player> roundPlayers = new ArrayList<>();
        int j;

        nextFirstPlayer = (nextFirstPlayer + 1)%size;
        //Add players
        for (j = nextFirstPlayer; j == Math.abs(nextFirstPlayer - 1)%size; j = (j+1)%size)
            roundPlayers.add(players.get(j));
        roundPlayers.add(players.get(Math.abs(nextFirstPlayer - 1)%size));

        //Add players in reverse order
        for (j = Math.abs(nextFirstPlayer-1)%size; j == nextFirstPlayer; j = Math.abs(j-1)%size)
            roundPlayers.add(players.get(j));
        roundPlayers.add(players.get(nextFirstPlayer));

        return roundPlayers;

    }

    public List<Schema> extractSchemas(){
        return this.dealer.extractSchemas(schemaPerPlayer);
    }
}