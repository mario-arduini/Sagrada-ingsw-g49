package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.controller.GameFlowHandler;
import it.polimi.ingsw.controller.GamesHandler;
import it.polimi.ingsw.controller.exceptions.GameNotStartedException;
import it.polimi.ingsw.controller.exceptions.GameOverException;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.server.ConnectionHandler;

public class FlowHandler extends GameFlowHandler implements FlowHandlerInterface {
    FlowHandler(GameFlowHandler gameFlow) {
        super(gameFlow);
    }

    @Override
    public void placeDice(int row, int column, Dice dice) throws GameNotStartedException, GameOverException, NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException {
        super.placeDice(row, column, dice);
        super.notifyDicePlaced(row, column, dice);
    }

}
