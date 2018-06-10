package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.controller.User;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.*;

import java.util.List;
import java.util.Random;

final class Effects {

    private Effects(){
        super();
    }

    static void getDraftedDice(Round round){
        Dice dice = askDiceDraftPool(round.getCurrentPlayer());
        round.getDraftPool().remove(dice);
        round.setCurrentDiceDrafted(dice);
        round.setDiceExtracted(true);
    }

    static void addDiceToWindow(Player player,Dice dice) throws BadAdjacentDiceException, ConstraintViolatedException, FirstDiceMisplacedException, NotWantedAdjacentDiceException, NoAdjacentDiceException {
        Coordinate coords = askDiceWindow("",player);
        placeDice(player,dice,coords.getRow(),coords.getColumn(), Window.RuleIgnored.NONE);
    }

    static void move(Player player,Window.RuleIgnored ruleIgnored){
        Coordinate start = null;
        Coordinate end = null;
        Window currentPlayerWindow = player.getWindow();
        Dice removedDice = null;
        boolean valid = false;
        String message = "move-from";
        while (!valid){
            valid = true;
            start = askDiceWindow(message, player);
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
            end = askDiceWindow(message, player);
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

    static void changeValue(Dice dice){
        dice.roll();
    }

    static void changeValue(Player player,Dice dice,int value) {
        String message = "ask-plus";
        boolean valid=false;
        while (!valid){
            valid = true;
            if (askIfPlus(message,player)){
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

        return;
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

    static void swapRoundTrack(Round round,List<Dice> roundTrack){
        boolean valid = false;
        int position=0;
        while(!valid){
            position = askDiceRoundTrack(round.getCurrentPlayer());
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

    static void getDiceFromBag(Round round,Dice dice){
        int value;
        boolean valid = false;
        while (!valid){
            valid = true;
            value = askDiceValue(round.getCurrentPlayer());
            try {
                dice.setValue(value);
            } catch (InvalidDiceValueException e) {
                valid = false;
            }
        }
        round.setCurrentDiceDrafted(dice);
    }

    static void placeDice(Player player,Dice dice, int row, int column, Window.RuleIgnored ruleIgnored) throws NotWantedAdjacentDiceException, ConstraintViolatedException, NoAdjacentDiceException, BadAdjacentDiceException, FirstDiceMisplacedException {
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
                if(!player.getWindow().isFirstDicePlaced())
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

    static Coordinate askDiceWindow(String message, Player player){
        return ((User) player).askDiceWindow();
    }

    static Dice askDiceDraftPool(Player player){
        return  ((User) player).askDiceDraftPool();
    }

    static int askDiceRoundTrack(Player player){
        return  ((User) player).askDiceRoundTrack();
    }

    static boolean askIfPlus(String message,Player player){
        return  ((User) player).askIfPlus();
    }

    static int askDiceValue(Player player){
        return ((User) player).askDiceValue();
    }

}
