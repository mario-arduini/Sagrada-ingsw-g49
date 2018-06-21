package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.controller.User;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;
import it.polimi.ingsw.network.server.ConnectionHandler;

import java.util.List;
import java.util.Random;

final class Effects {

    private Effects(){
        super();
    }

    static void getDraftedDice(Round round, ConnectionHandler connection){
        boolean valid = false;
        Dice dice = null;
        String prompt = "choose-drafted";
        while (!valid){
            dice = connection.askDiceDraftPool(prompt);
            prompt = "choose-drafted-invalid";
            if(round.getDraftPool().contains(dice)) valid=true;
        }
        round.getDraftPool().remove(dice);
        round.setCurrentDiceDrafted(dice);
        round.setDiceExtracted(true);
    }

    static boolean addDiceToWindow(Player player, Dice dice, ConnectionHandler connection) {
        if(player.getWindow().possiblePlaces(dice, Window.RuleIgnored.NONE)==0) return false;
        boolean valid = false;
        Coordinate coords;
        while (!valid){
            //TODO:Why is that message empty?
            coords = connection.askDiceWindow("");
            try{
                placeDice(player,dice,coords.getRow(),coords.getColumn(), Window.RuleIgnored.NONE);
                valid = true;
            } catch (ConstraintViolatedException | NotWantedAdjacentDiceException
                    |FirstDiceMisplacedException | BadAdjacentDiceException | NoAdjacentDiceException e) {
            }
        }
        return true;
    }

    static void move(Player player,Window.RuleIgnored ruleIgnored,boolean optional, ConnectionHandler connection){
        if(optional && !connection.askIfPlus("want-move")) return;
        Coordinate start = null;
        Coordinate end = null;
        Window currentPlayerWindow = player.getWindow();
        Dice removedDice = null;
        boolean valid = false;
        String message = "move-from";
        while (!valid){
            valid = true;
            start = connection.askDiceWindow(message);
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
            end = connection.askDiceWindow(message);
            if (start.getRow() == end.getRow() && start.getColumn() == end.getColumn()){
                valid = false;
                message = "move-to-same";
            } else try {
                placeDice(player,removedDice, end.getRow(), end.getColumn(), ruleIgnored);
            } catch (NoAdjacentDiceException | BadAdjacentDiceException
                    | FirstDiceMisplacedException | ConstraintViolatedException | NotWantedAdjacentDiceException e) {
                valid = false;
                message = "move-to-invalid";
            }
        }
    }

    public static Dice move(Player player, List<Dice> roundTrack, Dice old, Window.RuleIgnored ruleIgnored,boolean optional, ConnectionHandler connection) {
        if(optional && !connection.askIfPlus("want-move")) return null;
        Coordinate start = null;
        Coordinate end = null;
        Window currentPlayerWindow = player.getWindow();
        Dice removedDice = null;
        boolean valid = false;
        String message = "move-from";
        while (!valid){
            valid = true;
            start = connection.askDiceWindow(message);
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
            end = connection.askDiceWindow(message);
            if (start.getRow() == end.getRow() && start.getColumn() == end.getColumn()){
                valid = false;
                message = "move-to-same";
            } else try {
                placeDice(player,removedDice, end.getRow(), end.getColumn(), ruleIgnored);
            } catch (NoAdjacentDiceException | BadAdjacentDiceException
                    | FirstDiceMisplacedException | ConstraintViolatedException | NotWantedAdjacentDiceException e) {
                valid = false;
                message = "move-to-invalid";
            }
        }
        return removedDice;
    }

    static void changeValue(Dice dice){
        dice.roll();
    }

    static void changeValue(Dice dice, int value, ConnectionHandler connection) {
        String message = "ask-plus";
        boolean valid=false;
        while (!valid){
            valid = true;
            if (connection.askIfPlus(message)){
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

    static void swapRoundTrack(Round round,List<Dice> roundTrack, ConnectionHandler connection){
        boolean valid = false;
        int position=0;
        String prompt = "choose-round-swap";
        while(!valid){
            position = connection.askDiceRoundTrack(prompt);
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
    }

    static void getDiceFromBag(Round round, Dice dice, ConnectionHandler connection){
        int value;
        boolean valid = false;
        String prompt = "choose-value";
        while (!valid){
            valid = true;
            value = connection.askDiceValue(prompt);
            prompt = "choose-value-invalid";
            try {
                dice.setValue(value);
            } catch (InvalidDiceValueException e) {
                valid = false;
            }
        }
        round.setCurrentDiceDrafted(dice);
    }

    private static void placeDice(Player player,Dice dice, int row, int column, Window.RuleIgnored ruleIgnored) throws NotWantedAdjacentDiceException, ConstraintViolatedException, NoAdjacentDiceException, BadAdjacentDiceException, FirstDiceMisplacedException {
        switch (ruleIgnored){
            case COLOR:
                player.getWindow().checkValueConstraint(player.getWindow().getSchema().getConstraint(row, column),dice);
                player.getWindow().checkPlacementConstraint(row, column, dice);
                player.getWindow().setDice(row, column, dice);
                break;
            case NUMBER:
                player.getWindow().checkColorConstraint(player.getWindow().getSchema().getConstraint(row, column),dice);
                player.getWindow().checkPlacementConstraint(row, column, dice);
                player.getWindow().setDice(row, column, dice);
                break;
            case ADJACENCIES:
                player.getWindow().checkValueConstraint(player.getWindow().getSchema().getConstraint(row, column), dice);
                player.getWindow().checkColorConstraint(player.getWindow().getSchema().getConstraint(row, column), dice);
                if(player.getWindow().isFirstDicePlaced())
                    try {
                        player.getWindow().checkAdjacencies(row, column, dice);
                        throw new NotWantedAdjacentDiceException();
                    }catch (NoAdjacentDiceException e){
                        player.getWindow().setDice(row, column, dice);
                    }
                else
                    try {
                        player.getWindow().checkBorder(row, column);
                        throw new NotWantedAdjacentDiceException();
                    }catch (FirstDiceMisplacedException e){
                        player.getWindow().setDice(row, column, dice);
                    }
                break;
            case NONE:
                player.getWindow().addDice(row, column, dice);
                break;
        }
    }

}
