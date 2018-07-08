package it.polimi.ingsw.model;

import it.polimi.ingsw.server.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.model.goalcards.PublicGoal;
import it.polimi.ingsw.model.toolcards.ToolCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;

/**
 * Class representing a single Game of Sagrada
 */
public class Game {
    private final Factory dealer;
    private final List<Player> players;
    private List<Dice> roundTrack;
    private int trackIndex;
    private final List<ToolCard> toolCards;
    private final List<PublicGoal> publicGoals;
    private int nextFirstPlayer;
    private Round currentRound;
    private static final int SCHEMA_PER_PLAYER = 4;
    private boolean playing;
    private List<Dice> diceBag;

    /**
     * Initialize a new Game given the list of Players
     * @param playerList List of Player for this Game
     * @throws NoMorePlayersException signals there are no more player
     */
    public Game(List<Player> playerList) throws NoMorePlayersException {
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
            } catch (PrivateGoalAlreadySetException | OutOfCardsException e) {
                e.printStackTrace();
            }
        });

        int size = players.size();
        currentRound = new Round(dealer.extractPool(2*(size) + 1),createRoundPlayers());
        currentRound.nextPlayer();
        this.playing = false;
        this.diceBag = dealer.getDiceBag();
    }

    /**
     * set flag Playing
     * @param playing new value of the flag
     */
    protected void setPlaying(boolean playing){
        this.playing = playing;
    }

    /**
     * Check if playing
     * @return value of flag playing
     */
    public boolean getPlaying(){
        return playing;
    }

    /**
     * Get List of Players
     * @return List of Player of this Game
     */
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

    /**
     * Get the Public Goals for the Game
     * @return List of Public Goals
     */
    public List<PublicGoal> getPublicGoals() {
        return publicGoals;
    }

    /**
     * Get the round track
     * @return Ordered List of Dice representing the round track
     */
    public List<Dice> getRoundTrack() {
        return roundTrack;
    }

    /**
     * Add Dice to the Round Track
     * @param dice Dice to add
     */
    private void addDiceToTracker(Dice dice){
        this.roundTrack.add(new Dice(dice));
        trackIndex++;
    }

    /**
     * Get Player given his nickname
     * @param nick nickname of the wanted player
     * @return Player with the given nick
     */
    public Player getPlayerByNick(String nick){
        Optional<Player> playerFetched = players.stream().filter(player -> player.getNickname().equals(nick)).findFirst();

        if (!playerFetched.isPresent()) throw new NoSuchElementException();
        return playerFetched.get();
    }

    /**
     * Get current round
     * @return current Round
     */
    public Round getCurrentRound(){
        return this.currentRound;
    }

    /**
     * Try to place the Dice on current player window
     * @param row row of the cell
     * @param column column of the cell
     * @param dice Dice to place
     * @throws NoAdjacentDiceException signals that a non-first dice is placed not adjacent to another dice
     * @throws BadAdjacentDiceException signals that one of the orthogonal was of the same color or value of dice
     * @throws ConstraintViolatedException signals that a constraint of the schema was not respected
     * @throws FirstDiceMisplacedException signals that the first was not place in the proper position (border of the window)
     * @throws DiceNotInDraftPoolException signals that the Dice is not present in the Draft Pool
     * @throws DiceAlreadyExtractedException signals that a Dice was already extracted in the current turn
     * @throws DiceAlreadyHereException signals the cell is already occupied
     */
    public void placeDice(int row, int column, Dice dice) throws NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException, DiceAlreadyHereException {
        currentRound.useDice(row, column, dice);
    }

    /**
     * Suspend current player
     */
    protected void suspendCurrentPlayer(){
        currentRound.suspendPlayer();
    }

    /**
     * Suspend player by nickname
     * @param nickname nickname of the Player to suspend
     */
    protected void suspendPlayer(String nickname){
         getPlayerByNick(nickname).suspend();
    }

    /**
     * Compute a List of Scores
     * @return List of Scores computed
     */
    protected List<Score> computeFinalScores(){
        List <Score> scores = new ArrayList<>();
        int privateScore;
        for(Player player: this.players){
            AtomicInteger publicScore = new AtomicInteger();

            privateScore = player.getPrivateGoal().computeScore(player.getWindow());
            publicGoals.forEach(goal -> publicScore.getAndAdd(goal.computeScore(player.getWindow())));
            scores.add(new Score(player.getNickname(), publicScore.get(), privateScore, player.getFavorToken(), player.getWindow().getEmptySpaces(), currentRound.getPlayerPosition(player)));
        }
        return Score.sort(scores);
    }

    /**
     * Check if Game is finished
     * @return true if Game is finished, false otherwise
     */
    public boolean isGameFinished(){
        return trackIndex == 10 || players.stream().filter(p -> !p.isSuspended()).count()<2;
    }

    /**
     * Check if Game is started
     * @return true if Game is started, false otherwise
     */
    public boolean isGameStarted(){
        List<Player> inGamePlayers = getPlayers();
        for (Player p: inGamePlayers)
            if (p.getWindow()==null)
                return false;
        return true;
    }

    /**
     * Start the next Round
     */
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

    /**
     * Create the Turn's List of Players for the Round
     * @return List of Players for the Round
     */
    private synchronized List<Player> createRoundPlayers(){
        List<Player> roundPlayers = new ArrayList<>();
        int j;
        int size;
        size = players.size();

        nextFirstPlayer = (nextFirstPlayer + 1)%size;

        //Add players
        for (j = nextFirstPlayer; j < size; j++)
            roundPlayers.add(players.get(j));
        for (j = 0; j<nextFirstPlayer; j++)
            roundPlayers.add(players.get(j));

        return roundPlayers;

    }

    /**
     * Extract schemas from the factory
     * @return List of extracted Schemas
     */
    public List<Schema> extractSchemas(){
        return this.dealer.extractSchemas(SCHEMA_PER_PLAYER);
    }

    /**
     * Start a Transaction
     * @return Transaction Snapshot
     */
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