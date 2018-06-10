package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Round;
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
