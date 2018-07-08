package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.model.exceptions.*;

import java.util.List;

/**
 * Final class containing all the possible prerequisites required by a ToolCard
 */
final class Prerequisites {

    private Prerequisites(){
        super();
    }

    /**
     * Check if Player has the needed Favor Tokens
     * @param player Player to check
     * @param needed favor tokens required
     * @throws NotEnoughFavorTokenException signals that Player has not enough
     */
    static void checkFavorToken(Player player,int needed) throws NotEnoughFavorTokenException {
        if(player.getFavorToken()<needed) throw new NotEnoughFavorTokenException();
    }

    /**
     * Check if Player has at least 1 Dice in his window
     * @param player Player to check
     * @throws NoDiceInWindowException signals active Player has no Dice in his window, altough it is requested by the ToolCard
     */
    static void checkDiceInWindow(Player player) throws NoDiceInWindowException {
        if(!player.getWindow().isFirstDicePlaced()) throw new NoDiceInWindowException();
    }

    /**
     * Check if there is at least 1 Dice in roundtrack
     * @param roundTrack List of Dice to check
     * @throws NoDiceInRoundTrackException signals there are no dice in the round track, altough it is requested by the ToolCard
     */
    static void checkDiceInRoundTrack(List<Dice> roundTrack) throws NoDiceInRoundTrackException{
        if(roundTrack.size()==0) throw new NoDiceInRoundTrackException();

    }

    /**
     * Check if exist a Color that is present in both Dice's from Player Window and RoundTrack
     * @param player Player to check
     * @param roundTrack Roundtrack
     * @throws NoDiceInWindowException signals active Player has no Dice in his window, altough it is requested by the ToolCard
     * @throws NoDiceInRoundTrackException signals there are no dice in the round track, altough it is requested by the ToolCard
     * @throws NoSameColorDicesException signals active Player has no Dice of the requested Color in his window, altough it is requested by the ToolCard
     */
    static void checkSameColorInWindowAndRoundTrack(Player player,List<Dice> roundTrack) throws NoDiceInWindowException, NoDiceInRoundTrackException, NoSameColorDicesException {
        checkDiceInWindow(player);
        checkDiceInRoundTrack(roundTrack);
        for(Dice dice : roundTrack){
            for(int r=0;r< Window.ROW;r++)
                for (int c=0;c<Window.COLUMN;c++) {
                    Dice cell = player.getWindow().getCell(r, c);
                    if(cell!=null&&cell.getColor()==dice.getColor()) return;
                }
        }
        throw new NoSameColorDicesException();

    }

    /**
     * Check if it is current Player first turn
     * @param round current round
     * @throws NotYourFirstTurnException signals is not first turn of the active Player, altough it is requested by the ToolCard
     */
    static void checkFirstTurn(Round round) throws NotYourFirstTurnException {
        if(round.getCurrentPlayerIndex()>=round.getPlayersNumber()) throw new NotYourFirstTurnException();
    }

    /**
     * Check if it is current Player second turn
     * @param round current round
     * @throws NotYourSecondTurnException signals active Player is not in his second turn, altough it is requested by the ToolCard
     */
    static void checkSecondTurn(Round round) throws NotYourSecondTurnException {
        if(round.getCurrentPlayerIndex()<round.getPlayersNumber()) throw new NotYourSecondTurnException();
    }

    /**
     * Check if in this turn Dice has not been extracted
     * @param diceExtracted flag to check
     * @throws AlreadyDraftedException signals active Player has already extracted a Dice, altough it is requested by the ToolCard that he has not
     */
    static void checkBeforeDraft(boolean diceExtracted) throws AlreadyDraftedException {
        if(diceExtracted) throw new AlreadyDraftedException();
    }

    /**
     * Check if in this turn Dice has already been extracted
     * @param diceExtracted flag to check
     * @throws NotDraftedYetException signals active Player has not extracted a Dice yet, altough it is requested by the ToolCard
     */
    static void checkAfterDraft(boolean diceExtracted) throws NotDraftedYetException {
        if(!diceExtracted) throw new NotDraftedYetException();
    }

    /**
     * Check if some Dice in the Window can be move
     * @param player Player to check
     * @param ruleIgnored Possible rule to ignore
     * @throws NothingCanBeMovedException signals active Player has no Dices that can be moved in his window, altough it is requested by the ToolCard
     */
    static void checkMovable(Player player, Window.RuleIgnored ruleIgnored) throws NothingCanBeMovedException {
        Window window = player.getWindow();
        boolean valid = false;
        Dice dice = null;
        for(int r = 0;r<Window.ROW; r++)
            for(int c = 0;c<Window.COLUMN; c++){
                dice = window.getCell(r,c);
                if(dice != null){
                    window.removeDice(r,c);
                    int expectedMinimumPositions = 2;
                    try{
                        window.canBePlaced(dice,r,c,ruleIgnored);
                    } catch (NoAdjacentDiceException | BadAdjacentDiceException
                            | FirstDiceMisplacedException | ConstraintViolatedException | DiceAlreadyHereException e) {
                        expectedMinimumPositions = 1;
                    }
                    int possiblePositions = window.possiblePlaces(dice,ruleIgnored);
                    if(possiblePositions>=expectedMinimumPositions){
                        valid = true;
                    }
                    window.setDice(r,c,dice);
                }
            }

        if(!valid) throw new NothingCanBeMovedException();
    }

    /**
     * Check if Window has at least 2 Dices
     * @param window Window to check
     * @throws NotEnoughDiceToMoveException signals active Player has not enough Dices in his window, altough it is requested by the ToolCard
     */
    static void checkTwoDiceInWindow(Window window) throws NotEnoughDiceToMoveException{
        if (window.numOfDicePlaced() < 2) throw new NotEnoughDiceToMoveException();
    }
}
