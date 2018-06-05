package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.*;

final class Prerequisites {

    private Prerequisites(){
        super();
    }

    static void checkFavorToken(Player player,int needed) throws InvalidFavorTokenNumberException, NotEnoughFavorTokenException {
        player.useFavorToken(needed);
    }

    static void checkDiceInWindow(Player player) throws NoDiceInWindowException {
        if(player.getWindow().isFirstDice()) throw new NoDiceInWindowException();
    }

    static void checkDiceInRoundTrack(Game game) throws NoDiceInRoundTrackException{
        if(game.getRoundTrack().size()==0) throw new NoDiceInRoundTrackException();
    }

    static void checkSecondTurn(Round round) throws NotYourSecondTurnException {
        if(round.getCurrentPlayerIndex()<round.getPlayersNumber()) throw new NotYourSecondTurnException();
    }

    static void checkBeforeDraft(Round round) throws AlreadyDraftedException {
        if(round.getCurrentDiceDrafted()!=null) throw new AlreadyDraftedException();
    }
}
