package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.exceptions.GameNotStartedException;
import it.polimi.ingsw.controller.exceptions.GameOverException;
import it.polimi.ingsw.controller.exceptions.NoSuchToolCardException;
import it.polimi.ingsw.controller.exceptions.NotYourTurnException;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.client.model.Dice;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Connection extends Remote {
    void chooseSchema(Integer schema) throws RemoteException, GameNotStartedException, GameOverException, WindowAlreadySetException;
    void placeDice(int row, int column, Dice dice) throws RemoteException, GameOverException, NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException, NoSameColorDicesException, GameNotStartedException;
    void useToolCard(String name) throws RemoteException, GameNotStartedException, GameOverException, NoSuchToolCardException, InvalidDiceValueException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, NotWantedAdjacentDiceException, NoAdjacentDiceException, NotDraftedYetException, NotYourFirstTurnException, NothingCanBeMovedException, NoSameColorDicesException;
    void pass() throws RemoteException, GameNotStartedException, GameOverException, NotYourTurnException;
    void logout() throws  RemoteException;
}
