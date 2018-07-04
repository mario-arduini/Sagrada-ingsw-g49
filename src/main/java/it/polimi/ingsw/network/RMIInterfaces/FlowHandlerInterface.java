package it.polimi.ingsw.network.RMIInterfaces;

import it.polimi.ingsw.controller.exceptions.*;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.exceptions.*;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;

public interface FlowHandlerInterface extends Remote, Serializable {

    //List<String> getPlayers();
    void chooseSchema(Integer schemaNumber) throws RemoteException, GameNotStartedException, GameOverException, WindowAlreadySetException;
    void placeDice(int row, int column, Dice dice) throws RemoteException, GameOverException, NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException, NoSameColorDicesException, GameNotStartedException;
    void useToolCard(String cardName) throws RemoteException, GameNotStartedException, GameOverException, NoSuchToolCardException, InvalidDiceValueException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, NotWantedAdjacentDiceException, NoAdjacentDiceException, NotDraftedYetException, NotYourFirstTurnException, NothingCanBeMovedException, NoSameColorDicesException, ToolcardAlreadyUsedException, NotEnoughDiceToMoveException;
    void pass() throws RemoteException, GameNotStartedException, GameOverException, NotYourTurnException;
    void logout() throws RemoteException;
    void newGame() throws RemoteException;
}
