package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.controller.exceptions.DisconnectionException;
import it.polimi.ingsw.controller.exceptions.RollbackException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.RMIInterfaces.ClientInterface;
import it.polimi.ingsw.network.server.Logger;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;

final class Effects {

    private Effects(){
        super();
    }

    static void getDraftedDice(Round round, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
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

    static boolean addDiceToWindow(Window window, Dice dice, ClientInterface connection, Window.RuleIgnored ignore, boolean rollback) throws RollbackException, DisconnectionException {
        if(window.possiblePlaces(dice, ignore)==0) return false;

        boolean valid = false;
        Coordinate coords = null;
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
                } catch (ConstraintViolatedException | NotWantedAdjacentDiceException
                        | FirstDiceMisplacedException | BadAdjacentDiceException | NoAdjacentDiceException e) {
                    Logger.print("addDiceToWindow: " + e);
                }
            }
        }
        return true;
    }

    static void move(Window currentPlayerWindow,Window.RuleIgnored ruleIgnored,boolean optional, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
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
                        | FirstDiceMisplacedException | ConstraintViolatedException e) {
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
            } catch (NoAdjacentDiceException | BadAdjacentDiceException
                    | FirstDiceMisplacedException | ConstraintViolatedException | NotWantedAdjacentDiceException e) {
                valid = false;
                message = "move-to-invalid";
                Logger.print("move2: " + e);
            }
        }
    }

    public static Dice move(Window currentPlayerWindow, List<Dice> roundTrack, Dice old, Window.RuleIgnored ruleIgnored,boolean optional, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
        try {
            if(optional && !connection.askIfPlus("want-move", rollback)) return null;
        } catch (RollbackException e) {
            throw e;
        }catch (Exception e){
            throw new DisconnectionException();
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
                        | FirstDiceMisplacedException | ConstraintViolatedException e) {
                    expectedMinimumPositions = 1;
                    Logger.print("move3: " + e);
                }
                int possiblePositions = currentPlayerWindow.possiblePlaces(removedDice,ruleIgnored);
                if(possiblePositions<expectedMinimumPositions){
                    valid = false;
                    message = "move-from-unmovable";
                    currentPlayerWindow.setDice(start.getRow(),start.getColumn(),removedDice);
                }
                else if(old!=null&&old.getColor()!=removedDice.getColor()){
                    valid = false;
                    message = "move-from-different-color";
                    currentPlayerWindow.setDice(start.getRow(),start.getColumn(),removedDice);
                } else {
                    valid = false;
                    for(Dice dice : roundTrack)
                        if(dice.getColor()==removedDice.getColor()){
                            valid=true;
                        }
                    if(!valid){
                        message = "move-from-not-round-color";
                        currentPlayerWindow.setDice(start.getRow(),start.getColumn(),removedDice);
                    }
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
            } catch (NoAdjacentDiceException | BadAdjacentDiceException
                    | FirstDiceMisplacedException | ConstraintViolatedException | NotWantedAdjacentDiceException e) {
                valid = false;
                message = "move-to-invalid";
                Logger.print("move4: " + e);
            }
        }
        return removedDice;
    }

    static void changeValue(Dice dice, ClientInterface connection) throws DisconnectionException{
        dice.roll();
        try {
            connection.showDice(dice);
        } catch (Exception e) {
            throw new DisconnectionException();
        }
    }

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

    static void flip(Dice dice){
        try {
            dice.setValue(7-dice.getValue());
        } catch (InvalidDiceValueException e) {
            e.printStackTrace(); // it will never happen
        }
    }

    static void rerollPool(List<Dice> dices){
        Random random = new Random();
        dices.forEach(d -> {
            try {
                d.setValue(random.nextInt(6)+1);
            } catch (InvalidDiceValueException e) {
                e.printStackTrace();
            }
        });
    }

    static void swapRoundTrack(Round round,List<Dice> roundTrack, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
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

    static void setDiceFromBag(Round round, Dice dice, ClientInterface connection, boolean rollback) throws RollbackException, DisconnectionException {
        int value = 0; //TODO: remove this init
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

    private static void placeDice(Window window,Dice dice, int row, int column, Window.RuleIgnored ruleIgnored) throws NotWantedAdjacentDiceException, ConstraintViolatedException, NoAdjacentDiceException, BadAdjacentDiceException, FirstDiceMisplacedException {
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
