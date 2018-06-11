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
        if(roundTrack.size()==0) throw new NoDiceInRoundTrackException();
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
}
