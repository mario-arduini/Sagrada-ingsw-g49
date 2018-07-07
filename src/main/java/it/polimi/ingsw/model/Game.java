package it.polimi.ingsw.model;

import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.model.toolcards.ToolCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private List<Dice> diceBag;

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
        currentRound = new Round(dealer.extractPool(2*(size) + 1),createRoundPlayers());
        currentRound.nextPlayer();
        this.playing = false;
        this.diceBag = dealer.getDiceBag();
    }

    protected void setPlaying(boolean playing){
        this.playing = playing;
    }

    public boolean getPlaying(){
        return playing;
    }

    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Makes a copy of each Toolcard and gives them back to a GameFlowHandler as an ArrayList.
     * @return toolCardsCopy
     */
    public List<ToolCard> getToolCards() {
        List<ToolCard> toolCardsCopy = new ArrayList<>();
        for (ToolCard t: toolCards){
            toolCardsCopy.add(new ToolCard(t));
        }
        return toolCardsCopy;
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

    public void placeDice(int row, int column, Dice dice) throws NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException, DiceAlreadyHereException {
        currentRound.useDice(row, column, dice);
    }

    protected void suspendCurrentPlayer(){
        currentRound.suspendPlayer();
    }

    protected void suspendPlayer(String nickname){
         getPlayerByNick(nickname).suspend();
    }

    protected List<Score> computeFinalScores(){
        List <Score> scores = new ArrayList<>();
        BinaryOperator<Integer> adder = (n1, n2) -> n1 + n2;
        int privateScore;
        for(Player player: this.players){
            AtomicInteger publicScore = new AtomicInteger();

            privateScore = player.getPrivateGoal().computeScore(player.getWindow());
            publicGoals.forEach(goal -> publicScore.getAndAdd(goal.computeScore(player.getWindow())));
            scores.add(new Score(player.getNickname(), publicScore.get(), privateScore, player.getFavorToken(), player.getWindow().getEmptySpaces(), currentRound.getPlayerPosition(player)));
        }
        return Score.sort(scores);
    }

    public boolean isGameFinished(){
        return trackIndex == 10 || players.stream().filter(p -> !p.isSuspended()).count()<2;
    }

    public boolean isGameStarted(){
        List<Player> inGamePlayers = getPlayers();
        for (Player p: inGamePlayers)
            if (p.getWindow()==null)
                return false;
        return true;
    }

    //TODO: consider moving this method to GameRoom
    public void nextRound(){
        Round round = new Round(currentRound);
        List<Player> roundPlayers;
        List<Dice> draftPool;

        int size = players.size();
        roundPlayers = createRoundPlayers();

        draftPool = currentRound.getDraftPool();
        addDiceToTracker(draftPool.get((new Random()).nextInt(draftPool.size())));
        currentRound = new Round(dealer.extractPool(2*(size) + 1), roundPlayers);
        try {
            currentRound.nextPlayer();
        } catch (NoMorePlayersException e) {
        }
        if (isGameFinished()){
            currentRound = round;
        }
    }

    private synchronized List<Player> createRoundPlayers(){
        List<Player> roundPlayers = new ArrayList<>();
        int j, size;
        size = players.size();

        nextFirstPlayer = (nextFirstPlayer + 1)%size;

        //Add players
        for (j = nextFirstPlayer; j < size; j++)
            roundPlayers.add(players.get(j));
        for (j = 0; j<nextFirstPlayer; j++)
            roundPlayers.add(players.get(j));

        return roundPlayers;

    }

    public List<Schema> extractSchemas(){
        return this.dealer.extractSchemas(schemaPerPlayer);
    }

    public TransactionSnapshot beginTransaction(){
        return new TransactionSnapshot(this, diceBag);
    }

    /**
     * Gets a Transaction object as a gameCopy and the name of the toolcard that is being used.
     * Commits changes if toolcard exists, current player is the one in the transaction and has enough favorToken to buy it.
     * Handles usage of toolcards.
     * @param gameCopy
     * @param cardName
     * @throws NoSuchToolCardException
     * @throws InvalidFavorTokenNumberException
     * @throws PlayerSuspendedException
     * @throws NotEnoughFavorTokenException
     */
    public synchronized void commit(TransactionSnapshot gameCopy, String cardName) throws NoSuchToolCardException, InvalidFavorTokenNumberException, PlayerSuspendedException, NotEnoughFavorTokenException {
        if (!gameCopy.getRound().getCurrentPlayer().equals(getCurrentRound().getCurrentPlayer())){
            throw new PlayerSuspendedException();
        }

        ToolCard card = null;

        for (ToolCard t: toolCards){
            if (t.getName().equalsIgnoreCase(cardName))
                card = t;
        }

        if (card == null) throw new NoSuchToolCardException();

        roundTrack = gameCopy.getRoundTrack();
        currentRound = gameCopy.getRound();
        currentRound.getCurrentPlayer().setWindow(gameCopy.getWindow());
        diceBag = gameCopy.getDiceBag();
        getCurrentRound().getCurrentPlayer().useFavorToken(card.getUsed() ? 2 : 1);
        card.setUsed();
    }
}