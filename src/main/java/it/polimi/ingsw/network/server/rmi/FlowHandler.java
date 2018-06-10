package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.controller.GameFlowHandler;
import it.polimi.ingsw.controller.GamesHandler;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.exceptions.*;

public class FlowHandler extends GameFlowHandler implements FlowHandlerInterface {
    public FlowHandler(GamesHandler gamesHandler) {
        super(gamesHandler);
    }

    @Override
    public void placeDice(int row, int column, Dice dice) throws NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException{
        super.placeDice(row, column, dice);
        super.notifyDicePlaced(row, column, dice);
    }

}
