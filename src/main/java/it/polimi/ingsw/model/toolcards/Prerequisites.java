package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.model.exceptions.*;

import java.util.List;

final class Prerequisites {

    private Prerequisites(){
        super();
    }

    static void checkFavorToken(Player player,int needed) throws NotEnoughFavorTokenException {
        if(player.getFavorToken()<needed) throw new NotEnoughFavorTokenException();
    }

    static void checkDiceInWindow(Player player) throws NoDiceInWindowException {
        if(!player.getWindow().isFirstDicePlaced()) throw new NoDiceInWindowException();
    }

    static void checkDiceInRoundTrack(List<Dice> roundTrack) throws NoDiceInRoundTrackException{
        if(roundTrack.isEmpty()) throw new NoDiceInRoundTrackException();

    }

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

    static void checkFirstTurn(Round round) throws NotYourFirstTurnException {
        if(round.getCurrentPlayerIndex()>=round.getPlayersNumber()) throw new NotYourFirstTurnException();
    }

    static void checkSecondTurn(Round round) throws NotYourSecondTurnException {
        if(round.getCurrentPlayerIndex()<round.getPlayersNumber()) throw new NotYourSecondTurnException();
    }

    static void checkBeforeDraft(boolean diceExtracted) throws AlreadyDraftedException {
        if(diceExtracted) throw new AlreadyDraftedException();
    }

    static void checkAfterDraft(boolean diceExtracted) throws NotDraftedYetException {
        if(!diceExtracted) throw new NotDraftedYetException();
    }

    static void checkMovable(Player player, Window.RuleIgnored ruleIgnored) throws NothingCanBeMovedException {
        Window window = player.getWindow();
        boolean valid = false;
        Dice dice;
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

    static void checkTwoDiceInWindow(Window window) throws NotEnoughDiceToMoveException{
        if (window.numOfDicePlaced() < 2) throw new NotEnoughDiceToMoveException();
    }
}
