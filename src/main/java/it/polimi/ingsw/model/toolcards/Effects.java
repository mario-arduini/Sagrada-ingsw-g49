package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.server.controller.exceptions.DisconnectionException;
import it.polimi.ingsw.server.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.RmiInterfaces.ClientInterface;
import it.polimi.ingsw.server.Logger;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Final Class containing all possible effects applicable by a ToolCard
 */
public final class Effects {

    private Effects(){
        super();
    }

    /**
     * Draft a dice chosen by a connection (player) from the Draftpool
     * @param round current round
     * @param connection Connection that choose the Dice
     * @param rollback Possibility of RollBack
     * @throws RollbackException signals Connection asked for a rollback
     * @throws DisconnectionException signals active player disconnected
     */
    public static void getDraftedDice(Round round, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
        boolean valid = false;
        Dice dice = null;
        String prompt = "choose-drafted";
        while (!valid){
            try {
                dice = connection.askDiceDraftPool(prompt, rollback);
            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
            prompt = "choose-drafted-invalid";
            if(round.getDraftPool().contains(dice)) valid=true;
        }
        round.getDraftPool().remove(dice);
        round.setCurrentDiceDrafted(dice);
        round.setDiceExtracted(true);
    }

    /**
     * Ask the Connection to place the Dice if possible
     * @param window Window in which place the dice
     * @param dice Dice to be placed
     * @param connection Connection that choose the position
     * @param ignore possible Rule to ignore in placing the Dice
     * @param rollback possibility of rollback
     * @return true if there is a possible place where the Dice can be put, false otherwise
     * @throws RollbackException signals Connection asked for a rollback
     * @throws DisconnectionException signals active player disconnected
     */
    public static boolean addDiceToWindow(Window window, Dice dice, ClientInterface connection, Window.RuleIgnored ignore, boolean rollback) throws RollbackException, DisconnectionException {
        if(window.possiblePlaces(dice, ignore)==0) return false;

        boolean valid = false;
        Coordinate coords;
        String prompt = "place-dice";
        while (!valid){
            try {
                coords = connection.askDiceWindow(prompt, rollback);
            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
            if (coords!= null) {
                prompt = "place-dice-invalid";
                try {
                    placeDice(window, dice, coords.getRow(), coords.getColumn(), ignore);
                    valid = true;
                } catch (ConstraintViolatedException | NotWantedAdjacentDiceException |
                        FirstDiceMisplacedException | BadAdjacentDiceException |
                        NoAdjacentDiceException | IndexOutOfBoundsException |
                        DiceAlreadyHereException e) {
                    Logger.print("addDiceToWindow: " + e);
                }
            }
        }
        return true;
    }


    /**
     * Ask the Connection to move n (or up to n Dice if optional is true) Dice in his window
     * @param n number of Dice to move
     * @param currentPlayerWindow Window to use
     * @param ignored Possible Rule to ignore
     * @param optional if true the player has to move exactly n Dice, otherwise can choose up to n Dice
     * @param connection Connection that choose the Dice
     * @param rollback Possibility of RollBack
     * @throws RollbackException signals Connection asked for a rollback
     * @throws DisconnectionException signals active player disconnected
     */
    static void moveN (int n , Window currentPlayerWindow, Window.RuleIgnored ignored, boolean optional, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException{
        String prompt;
        //If optional asks number of moves till it's <= n
        int num = n;
        if (optional) {
            num += 1;
            prompt = "move-number";
            while (num > n) {
                try {
                    num = connection.askMoveNumber(prompt, n, rollback);
                } catch (RollbackException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DisconnectionException();
                }
                prompt = "move-number-invalid";
            }
        }

        //Fetch dice
        List<Dice> diceList = new ArrayList<>();
        List<Coordinate> start = new ArrayList<>();
        Dice removedDice;

        prompt = "move-from";
        while(num > diceList.size()){
            try {
                start.add(connection.askDiceWindow(prompt, rollback));
                removedDice = currentPlayerWindow.getCell(start.get(diceList.size()).getRow(),start.get(diceList.size()).getColumn());
                if(removedDice == null){
                    start.remove(diceList.size());
                    prompt = "move-from-empty";
                }else {
                    currentPlayerWindow.removeDice(start.get(diceList.size()).getRow(),start.get(diceList.size()).getColumn());
                    diceList.add(removedDice);
                    prompt = "move-from";
                }

            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
        }

        //Place dice
        Coordinate end;
        prompt = "move-to";
        while (diceList.size() > 0) {
            try {
                end = connection.askDiceWindow(prompt, rollback);
                if (start.get(num - diceList.size()).getRow() == end.getRow() && start.get(num - diceList.size()).getColumn() == end.getColumn())
                    prompt = "move-to-same";
                else {
                    try {
                        removedDice = diceList.get(0);
                        placeDice(currentPlayerWindow,removedDice, end.getRow(), end.getColumn(), ignored);
                        diceList.remove(0);
                        prompt = "move-to";
                    }catch (NoAdjacentDiceException | BadAdjacentDiceException
                            | FirstDiceMisplacedException | ConstraintViolatedException | NotWantedAdjacentDiceException e) {
                        prompt = "move-to-invalid";
                    }
                }
            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
        }
    }

    /**
     * Ask the Connection to move n (or up to n Dice if optional is true) Dice of the same Color of a Dice in the RoundTrack in his window
     * @param n number of Dice to move
     * @param currentPlayerWindow Window to use
     * @param roundTrack List of Dice providing the possibles Colors
     * @param ignored Possible Rule to ignore
     * @param optional if true the player has to move exactly n Dice, otherwise can choose up to n Dice
     * @param connection Connection that choose the Dice
     * @param rollback Possibility of RollBack
     * @throws RollbackException signals Connection asked for a rollback
     * @throws DisconnectionException signals active player disconnected
     */
    static void moveNColor (int n , Window currentPlayerWindow, List<Dice> roundTrack, Window.RuleIgnored ignored, boolean optional, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException{
        String prompt;
        //If optional asks number of moves till it's <= n
        int num = n;
        if (optional) {
            num += 1;
            prompt = "move-number";
            while (num > n && num > 0) {
                try {
                    num = connection.askMoveNumber(prompt, n, rollback);
                } catch (RollbackException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DisconnectionException();
                }
                prompt = "move-number-invalid";
            }
        }

        //Fetch dice
        List<Dice> diceList = new ArrayList<>();
        List<Coordinate> start = new ArrayList<>();
        Dice removedDice;

        prompt = "move-from";
        while(num > diceList.size()){
            try {
                start.add(connection.askDiceWindow(prompt, rollback));
                removedDice = currentPlayerWindow.getCell(start.get(diceList.size()).getRow(),start.get(diceList.size()).getColumn());
                if(removedDice == null){
                    prompt = "move-from-empty";
                    start.remove(diceList.size());
                }else {
                    if (diceList.isEmpty()){
                        if (removedDice.getColor().equals(diceList.get(0).getColor())){
                            currentPlayerWindow.removeDice(start.get(diceList.size()).getRow(), start.get(diceList.size()).getColumn());
                            diceList.add(removedDice);
                            prompt = "move-from";
                        }else{
                            start.remove(diceList.size());
                            prompt = "move-from-different-color";
                        }
                    }else{
                        boolean flag = false;
                        for (Dice dice: roundTrack)
                            if (dice.getColor().equals(removedDice.getColor()))
                                flag = true;
                        if (flag){
                            currentPlayerWindow.removeDice(start.get(diceList.size()).getRow(), start.get(diceList.size()).getColumn());
                            diceList.add(removedDice);
                            prompt = "move-from";
                        }else {
                            start.remove(diceList.size());
                            prompt = "move-from-not-round-color";
                        }
                    }
                }

            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
        }

        //Place dice
        Coordinate end;
        prompt = "move-to";
        while (diceList.size() > 0) {
            try {
                end = connection.askDiceWindow(prompt, rollback);
                if (start.get(num - diceList.size()).getRow() == end.getRow() && start.get(num - diceList.size()).getColumn() == end.getColumn())
                    prompt = "move-to-same";
                else {
                    try {
                        removedDice = diceList.get(0);
                        placeDice(currentPlayerWindow,removedDice, end.getRow(), end.getColumn(), ignored);
                        diceList.remove(0);
                        prompt = "move-to";
                    }catch (NoAdjacentDiceException | BadAdjacentDiceException
                            | FirstDiceMisplacedException | ConstraintViolatedException | NotWantedAdjacentDiceException e) {
                        prompt = "move-to-invalid";
                    }
                }
            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
        }
    }

    /**
     * Ask the Connection to move a Dice in his window
     * @param currentPlayerWindow Window to use
     * @param ruleIgnored Possible Rule to ignore
     * @param optional if true the player has to move exactly n Dice, otherwise can choose up to n Dice
     * @param connection Connection that choose the Dice
     * @param rollback Possibility of RollBack
     * @throws RollbackException signals Connection asked for a rollback
     * @throws DisconnectionException signals active player disconnected
     */
    public static void move(Window currentPlayerWindow,Window.RuleIgnored ruleIgnored,boolean optional, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
        try {
            if(optional && !connection.askIfPlus("want-move", rollback)) return;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Coordinate start = null;
        Coordinate end = null;
        Dice removedDice = null;
        boolean valid = false;
        String message = "move-from";
        while (!valid){
            valid = true;
            try {
                start = connection.askDiceWindow(message, rollback);
            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
            removedDice = currentPlayerWindow.getCell(start.getRow(),start.getColumn());
            if(removedDice == null){
                valid = false;
                message = "move-from-empty";
            }
            else{
                currentPlayerWindow.removeDice(start.getRow(),start.getColumn());
                int expectedMinimumPositions = 2;
                try{
                    currentPlayerWindow.canBePlaced(removedDice,start.getRow(),start.getColumn(),ruleIgnored);
                } catch (NoAdjacentDiceException | BadAdjacentDiceException
                        | FirstDiceMisplacedException | ConstraintViolatedException | DiceAlreadyHereException e) {
                    expectedMinimumPositions = 1;
                    Logger.print("move1: " + e);
                }
                int possiblePositions = currentPlayerWindow.possiblePlaces(removedDice,ruleIgnored);
                if(possiblePositions<expectedMinimumPositions){
                    valid = false;
                    message = "move-from-unmovable";
                    currentPlayerWindow.setDice(start.getRow(),start.getColumn(),removedDice);
                }
            }
        }
        valid = false;
        message = "move-to";
        while (!valid) {
            valid = true;
            try {
                end = connection.askDiceWindow(message, rollback);
            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
            if (start.getRow() == end.getRow() && start.getColumn() == end.getColumn()){
                valid = false;
                message = "move-to-same";
            } else try {
                placeDice(currentPlayerWindow,removedDice, end.getRow(), end.getColumn(), ruleIgnored);
            } catch (NoAdjacentDiceException | BadAdjacentDiceException |
                    FirstDiceMisplacedException | ConstraintViolatedException |
                    DiceAlreadyHereException | NotWantedAdjacentDiceException e) {
                valid = false;
                message = "move-to-invalid";
                Logger.print("move2: " + e);
            }
        }
    }

    /**
     * Reroll the Dice and notify the connection the result
     * @param dice Dice to be rerolled
     * @param connection Connection to notify
     * @throws DisconnectionException signals Player has disconnected
     */
    static void changeValue(Dice dice, ClientInterface connection) throws DisconnectionException{
        dice.roll();
        try {
            connection.showDice(dice);
        } catch (Exception e) {
            throw new DisconnectionException();
        }
    }

    /**
     * Ask the connection to change the value of the Dice by value
     * @param dice Dice to be changed
     * @param value Value that can be added or removed
     * @param connection Connection that choose the Dice
     * @param rollback Possibility of RollBack
     * @throws RollbackException signals Connection asked for a rollback
     * @throws DisconnectionException signals active player disconnected
     */
    static void changeValue(Dice dice, int value, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
        String message = "ask-plus";
        boolean valid=false;
        while (!valid){
            valid = true;
            try {
                if (connection.askIfPlus(message, rollback)){
                    try {
                        dice.setValue(dice.getValue()+value);
                    } catch (InvalidDiceValueException e) {
                        valid=false;
                        message = "ask-plus-invalid";
                    }
                } else {
                    try {
                        dice.setValue(dice.getValue()-value);
                    } catch (InvalidDiceValueException e) {
                        valid = false;
                        message = "ask-plus-invalid";
                    }
                }
            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
        }
        try {
            connection.showDice(dice);
        } catch (Exception e) {
            throw new DisconnectionException();
        }
    }

    /**
     * Flip the Dice and notify connection
     * @param dice Dice to be flipped
     * @param connection Connection to be notified
     * @throws DisconnectionException signals player has disconnected
     */
    public static void flip(Dice dice, ClientInterface connection) throws DisconnectionException{
        try {
            dice.setValue(7-dice.getValue());
            connection.showDice(dice);
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }catch (Exception e){
            throw new DisconnectionException();
        }
    }

    /**
     * Reroll the List of given Dices
     * @param dices List of Dices to reroll
     */
    public static void rerollPool(List<Dice> dices){
        Random random = new Random();
        dices.forEach(d -> {
            try {
                d.setValue(random.nextInt(6)+1);
            } catch (InvalidDiceValueException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Ask the player to draft a Dice and change it with a Dice of the RoundTrack
     * @param round current Round
     * @param roundTrack RoundTrack
     * @param connection Connection that choose the Dice
     * @param rollback Possibility of RollBack
     * @throws RollbackException signals Connection asked for a rollback
     * @throws DisconnectionException signals active player disconnected
     */
    public static void swapRoundTrack(Round round,List<Dice> roundTrack, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
        boolean valid = false;
        int position=0;
        String prompt = "choose-round-swap";
        while(!valid){
            try {
                position = connection.askDiceRoundTrack(prompt, rollback);
            } catch (RollbackException e){
                throw new RollbackException();
            } catch (Exception e) {
                throw new DisconnectionException();
            }
            prompt = "choose-round-swap-invalid";
            valid = true;
            try{
                roundTrack.get(position);
            } catch (IndexOutOfBoundsException e){
                valid = false;
            }
        }
        Dice toSwap = roundTrack.get(position);
        roundTrack.set(position,round.getCurrentDiceDrafted());
        round.setCurrentDiceDrafted(toSwap);
        round.getDraftPool().add(round.getCurrentDiceDrafted());
    }

    /**
     * Ask the Connection to choose a value for a Dice extracted from the bag
     * @param round current round
     * @param dice Dice extracted
     * @param connection Connection that choose the Dice
     * @param rollback Possibility of RollBack
     * @throws RollbackException signals Connection asked for a rollback
     * @throws DisconnectionException signals active player disconnected
     */
    public static void setDiceFromBag(Round round, Dice dice, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
        int value;
        boolean valid = false;
        String prompt = "choose-value";

        //TODO: what if disconnection here?!
        try {
            connection.showDice(dice);
        }catch (Exception e){
            throw new DisconnectionException();
        }

        while (!valid){
            valid = true;
            try {
                value = connection.askDiceValue(prompt, rollback);
            } catch (RollbackException e) {
                throw e;
            }catch (Exception e){
                throw new DisconnectionException();
            }
            prompt = "choose-value-invalid";
            try {
                dice.setValue(value);
            } catch (InvalidDiceValueException e) {
                valid = false;
            }
        }
        round.setCurrentDiceDrafted(dice);
    }

    /**
     * try to place a dice in the given cell, possibly ignoring some restrictions or enforcing others
     * @param window Window in which place the Dice
     * @param dice Dice to place
     * @param row row of the cell
     * @param column column of the cell
     * @param ruleIgnored possible rule to ignore
     * @throws NotWantedAdjacentDiceException signals Dice is adjacent to another Dice, but shouldn't be
     * @throws ConstraintViolatedException signals that a constraint of the schema was not respected
     * @throws FirstDiceMisplacedException signals that the first was not place in the proper position (border of the window)
     * @throws NoAdjacentDiceException signals that a non-first dice is placed not adjacent to another dice
     * @throws BadAdjacentDiceException signals that one of the orthogonal was of the same color or value of dice
     * @throws DiceAlreadyHereException signals that the cell is already occupied by another dice
     */
    private static void placeDice(Window window,Dice dice, int row, int column, Window.RuleIgnored ruleIgnored) throws NotWantedAdjacentDiceException, ConstraintViolatedException, NoAdjacentDiceException, BadAdjacentDiceException, FirstDiceMisplacedException, DiceAlreadyHereException{
        switch (ruleIgnored){
            case COLOR:
                window.checkValueConstraint(window.getSchema().getConstraint(row, column),dice);
                window.checkPlacementConstraint(row, column, dice);
                window.setDice(row, column, dice);
                break;
            case NUMBER:
                window.checkColorConstraint(window.getSchema().getConstraint(row, column),dice);
                window.checkPlacementConstraint(row, column, dice);
                window.setDice(row, column, dice);
                break;
            case ADJACENCIES:
                window.checkValueConstraint(window.getSchema().getConstraint(row, column), dice);
                window.checkColorConstraint(window.getSchema().getConstraint(row, column), dice);
                if(window.isFirstDicePlaced())
                    try {
                        window.checkAdjacencies(row, column, dice);
                        throw new NotWantedAdjacentDiceException();
                    }catch (NoAdjacentDiceException e){
                        window.setDice(row, column, dice);
                    }
                else
                    window.setDice(row, column, dice);
                break;
            case NONE:
                window.addDice(row, column, dice);
                break;
        }
    }

}