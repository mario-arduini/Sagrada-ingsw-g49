package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.model.toolcards.ToolCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;

public class Game {
    private final Factory dealer;
    private final List<Player> players;
    private List<Dice> roundTrack;
    private int trackIndex;
    private final List<ToolCard> toolCards;
    private final List<PublicGoal> publicGoals;
    private int nextFirstPlayer;
    private Round currentRound;
    private static final int schemaPerPlayer = 4;
    private boolean playing;

    public Game(List<Player> playerList) throws NoMorePlayersException { //Fix UML for players
        this.dealer = new Factory();
        this.roundTrack = new ArrayList<>();
        this.toolCards = new ArrayList<>();
        this.trackIndex = 0;
        this.publicGoals = new ArrayList<>();
        this.players = new ArrayList<>();
        this.players.addAll(playerList);
        for (int j = 0; j<3; j++) {
            try {
                this.publicGoals.add(dealer.extractPublicGoal());
            } catch (OutOfCardsException e) {
                e.printStackTrace();
            }
        }
        for (int j = 0; j<3; j++) {
            try {
                this.toolCards.add(dealer.extractToolCard());
            } catch (OutOfCardsException e) {
                e.printStackTrace();
            }
        }
        nextFirstPlayer = (new Random()).nextInt(players.size());

        playerList.forEach(player -> {
            try {
                player.setPrivateGoal(dealer.extractPrivateGoal());
            } catch (PrivateGoalAlreadySetException e) {
                e.printStackTrace();
            } catch (OutOfCardsException e) {
                e.printStackTrace();
            }
        });

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

    public List<ToolCard> getToolCards() {
        return toolCards;
    }

    public List<PublicGoal> getPublicGoals() {
        return publicGoals;
    }

    public List<Dice> getRoundTrack() {
        return roundTrack;
    }

    public List<Dice> getDiceFromPool(){
        return currentRound.getDraftPool();
    }

    private void addDiceToTracker(Dice dice){
        this.roundTrack.add(new Dice(dice));
        trackIndex++;
    }

    public Player getPlayerByNick(String nick){
        Optional<Player> playerFetched = players.stream().filter(player -> player.getNickname().equals(nick)).findFirst();

        if (!playerFetched.isPresent()) throw new NoSuchElementException();
        return playerFetched.get();
    }

    //TODO: Fix UML
    public Round getCurrentRound(){
        return this.currentRound;
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

    //public

    //TODO: compute final score for each player and handle draws
    public int computeFinalScore(Player player){
        BinaryOperator<Integer> adder = (n1, n2) -> n1 + n2;
        BinaryOperator<Integer> negAdder = (n1, n2) -> n1 - n2;
        AtomicReference<Integer> score = new AtomicReference<>();
        score.getAndSet(player.getPrivateGoal().computeScore(player.getWindow()));
        publicGoals.forEach(goal -> score.getAndAccumulate(goal.computeScore(player.getWindow()), adder));
        score.getAndAccumulate(player.getFavorToken(), adder);
        score.getAndAccumulate(player.getWindow().getEmptySpaces(), negAdder);
        return score.get();
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
        try {
            currentRound.nextPlayer();
        } catch (NoMorePlayersException e) {
            e.printStackTrace();
        }
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

    public void putInBag(Dice dice){
        dealer.putInBag(dice);
    }

    public Dice getFromBag(){
        return dealer.getFromBag();
    }
}