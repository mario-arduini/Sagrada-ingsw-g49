package it.polimi.ingsw.network.server.rmi;

import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.exceptions.*;

import java.util.List;

public interface FlowHandlerInterface extends java.io.Serializable{

    List<String> getPlayers();
    void chooseSchema(Integer schemaNumber);
    void placeDice(int row, int column, Dice dice) throws NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException, NoSameColorDicesException;
    void useToolCard(String cardName) throws NoSuchToolCardException, InvalidDiceValueException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, NotWantedAdjacentDiceException, NoAdjacentDiceException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException;
    void pass() throws NotYourTurnException;
    void logout();
}
