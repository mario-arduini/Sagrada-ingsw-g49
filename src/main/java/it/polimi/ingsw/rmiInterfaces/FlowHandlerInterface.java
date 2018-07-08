package it.polimi.ingsw.rmiInterfaces;

import it.polimi.ingsw.server.controller.exceptions.*;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.exceptions.*;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface that allows a player to interact with the game.
 * Class that implement this interface should take care of the Game Flow.
 */
public interface FlowHandlerInterface extends Remote, Serializable {

    /**
     * Allows a player to choose a schema and set his window.
     * @param schemaNumber Number of schema chosen.
     * @throws RemoteException on RMI problems.
     * @throws GameNotStartedException if game is not started yet.
     * @throws GameOverException if game is over.
     * @throws WindowAlreadySetException if player had already chosen the schema.
     */
    void chooseSchema(Integer schemaNumber) throws RemoteException, GameNotStartedException, GameOverException, WindowAlreadySetException;

    /**
     * Allows a player to place a dice in his window.
     * @param row row of the window where the dice has to be placed.
     * @param column column of the window where the dice has to be placed.
     * @param dice the dice to be placed.
     * @throws RemoteException on RMI problems.
     * @throws GameOverException if game is over.
     * @throws NotYourTurnException if it's not the turn of the player calling the method.
     * @throws NoAdjacentDiceException if there is no adjacent dice.
     * @throws DiceAlreadyExtractedException if dice has already been extracted.
     * @throws BadAdjacentDiceException if constraints of adjacency are not satisfied.
     * @throws FirstDiceMisplacedException if constraints of first placement are not satisfied.
     * @throws ConstraintViolatedException if constraints of schema are not satisfied.
     * @throws DiceNotInDraftPoolException if dice requested is not present in draft pool.
     * @throws GameNotStartedException if game is not started yet.
     * @throws ToolCardInUseException if a Tool Card needs to be finished using.
     * @throws DiceAlreadyHereException if there's already a dice in the window.
     */
    void placeDice(int row, int column, Dice dice) throws RemoteException, GameOverException, NotYourTurnException, NoAdjacentDiceException, DiceAlreadyExtractedException, BadAdjacentDiceException, FirstDiceMisplacedException, ConstraintViolatedException, DiceNotInDraftPoolException, GameNotStartedException, ToolCardInUseException, DiceAlreadyHereException;

    /**
     * Allows a player to use a tool card.
     * @param cardName the name of the card to be used.
     * @throws RemoteException on RMI problems.
     * @throws GameNotStartedException if game is not started yet.
     * @throws GameOverException if game is over.
     * @throws NoSuchToolCardException if desired tool card does not exists.
     * @throws ToolcardAlreadyUsedException if a tool card has already been used in this turn.
     * @throws NotYourSecondTurnException if tool card has to be played on his second turn but it's not his second turn.
     * @throws AlreadyDraftedException if tool card has to be used before draft and the player already drafted a dice.
     * @throws NoDiceInRoundTrackException if there is no dice in the round track but the tool card needs it.
     * @throws InvalidFavorTokenNumberException if player hasn't got enough favor token to play the tool card.
     * @throws NotEnoughFavorTokenException if player hasn't got enough favor token to play the tool card.
     * @throws NoDiceInWindowException if there is no dice in the window but the tool card needs it.
     * @throws NotYourTurnException if it's not the turn of the player that called the method.
     * @throws NotDraftedYetException if tool card needs to be played after draft and the player hasn't already drafted a dice.
     * @throws NotYourFirstTurnException if tool card has to be played on his first turn but it's not his first turn.
     * @throws NoSameColorDicesException if tool card requires a dice with the same color and it's missing.
     * @throws NothingCanBeMovedException if nothing can be moved.
     * @throws NotEnoughDiceToMoveException if player's window hasn't got enough dice to move.
     * @throws PlayerSuspendedException if player had been suspended during a tool card.
     * @throws ToolCardInUseException if there already is a tool card in use.
     */
    void useToolCard(String cardName) throws RemoteException, GameNotStartedException, GameOverException, NoSuchToolCardException, ToolcardAlreadyUsedException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException, NotEnoughDiceToMoveException, PlayerSuspendedException, ToolCardInUseException;

    /**
     * Allows a player to pass during his turn.
     * @throws RemoteException on RMI problems.
     * @throws GameNotStartedException if game has not started yet.
     * @throws GameOverException if game has already finished.
     * @throws NotYourTurnException if it's not the turn of the player that called the method.
     */
    void pass() throws RemoteException, GameNotStartedException, GameOverException, NotYourTurnException;

    /**
     * Allows a player to continue using his tool card if he was disconnected.
     * @throws RemoteException on RMI problems.
     * @throws GameNotStartedException if game is not started yet.
     * @throws GameOverException if game is over.
     * @throws NoSuchToolCardException if desired tool card does not exists.
     * @throws ToolcardAlreadyUsedException if a tool card has already been used in this turn.
     * @throws NotYourSecondTurnException if tool card has to be played on his second turn but it's not his second turn.
     * @throws AlreadyDraftedException if tool card has to be used before draft and the player already drafted a dice.
     * @throws NoDiceInRoundTrackException if there is no dice in the round track but the tool card needs it.
     * @throws InvalidFavorTokenNumberException if player hasn't got enough favor token to play the tool card.
     * @throws NotEnoughFavorTokenException if player hasn't got enough favor token to play the tool card.
     * @throws NoDiceInWindowException if there is no dice in the window but the tool card needs it.
     * @throws NotYourTurnException if it's not the turn of the player that called the method.
     * @throws NotDraftedYetException if tool card needs to be played after draft and the player hasn't already drafted a dice.
     * @throws NotYourFirstTurnException if tool card has to be played on his first turn but it's not his first turn.
     * @throws NoSameColorDicesException if tool card requires a dice with the same color and it's missing.
     * @throws NothingCanBeMovedException if nothing can be moved.
     * @throws NotEnoughDiceToMoveException if player's window hasn't got enough dice to move.
     * @throws PlayerSuspendedException if player had been suspended during a tool card.
     */
    void continueToolCard() throws RemoteException, GameNotStartedException,  GameOverException, NoSuchToolCardException, ToolcardAlreadyUsedException, NotYourSecondTurnException, AlreadyDraftedException, NoDiceInRoundTrackException, InvalidFavorTokenNumberException, NotEnoughFavorTokenException, NoDiceInWindowException, NotYourTurnException, NotDraftedYetException, NotYourFirstTurnException, NoSameColorDicesException, NothingCanBeMovedException, NotEnoughDiceToMoveException, PlayerSuspendedException;

    /**
     * Allows a player to log out.
     * @throws RemoteException on RMI problems.
     */
    void logout() throws RemoteException;

    /**
     * Allows a player to be put again in the waiting room and leave the current game.
     * @throws RemoteException on RMI problems.
     */
    void newGame() throws RemoteException;
}
