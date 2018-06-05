package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.Coordinate;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.model.exceptions.*;

import java.util.List;
import java.util.Random;

final class Effects {

    enum RuleIgnored{ COLOR, NUMBER, ADJACENCIES, NONE }

    enum GetDiceFrom{ DRAFT_POOL, BAG, ROUND_TRACK}

    private Effects(){
        super();
    }

    static void flip(Dice dice){
        try {
            dice.setValue(7-dice.getValue());
        } catch (InvalidDiceValueException e) {
            e.printStackTrace(); // it will never happen
        }
    }

    static void changeValue(Dice dice){
        try {
            dice.setValue((new Random()).nextInt(7));
        } catch (InvalidDiceValueException e) {
            e.printStackTrace();
        }
    }

    static void changeValue(List<Dice> dice){
        Random random = new Random();
        dice.forEach(d -> {
            try {
                d.setValue(random.nextInt(7));
            } catch (InvalidDiceValueException e) {
                e.printStackTrace();
            }
        });
    }

    static void changeValue(Dice dice,boolean isPlus,int value) throws InvalidDiceValueException {
        if (isPlus) dice.setValue(dice.getValue()+value);
        else dice.setValue(dice.getValue()-value);
        return;
    }

    static void changeValue(Dice dice,int value) throws InvalidDiceValueException {
        dice.setValue(value);
        return;
    }

    static void placeDice(Round round, int row, int column, RuleIgnored ruleIgnored) throws NotWantedAdjacentDiceException, ConstraintViolatedException, NoAdjacentDiceException, BadAdjacentDiceException, FirstDiceMisplacedException {
        switch (ruleIgnored){
            case COLOR:
                round.getCurrentPlayer().getWindow().checkValueConstraint(round.getCurrentPlayer().getWindow().getSchema().getConstraint(row, column), round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().checkPlacementConstraint(row, column, round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().setDice(row, column, round.getCurrentDiceDrafted());
                break;
            case NUMBER:
                round.getCurrentPlayer().getWindow().checkColorConstraint(round.getCurrentPlayer().getWindow().getSchema().getConstraint(row, column), round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().checkPlacementConstraint(row, column, round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().setDice(row, column, round.getCurrentDiceDrafted());
                break;
            case ADJACENCIES:
                round.getCurrentPlayer().getWindow().checkValueConstraint(round.getCurrentPlayer().getWindow().getSchema().getConstraint(row, column), round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().checkColorConstraint(round.getCurrentPlayer().getWindow().getSchema().getConstraint(row, column), round.getCurrentDiceDrafted());
                if(!round.getCurrentPlayer().getWindow().isFirstDice())
                    try {
                        round.getCurrentPlayer().getWindow().checkAdjacencies(row, column, round.getCurrentDiceDrafted());
                        throw new NotWantedAdjacentDiceException();
                    }catch (NoAdjacentDiceException e){
                        round.getCurrentPlayer().getWindow().setDice(row, column, round.getCurrentDiceDrafted());
                    }
                else
                    try {
                        round.getCurrentPlayer().getWindow().checkBorder(row, column);
                        throw new NotWantedAdjacentDiceException();
                    }catch (FirstDiceMisplacedException e){
                        round.getCurrentPlayer().getWindow().setDice(row, column, round.getCurrentDiceDrafted());
                    }
                break;
            case NONE:
                round.getCurrentPlayer().getWindow().addDice(row, column, round.getCurrentDiceDrafted());
                break;
        }
    }

    static Coordinate askPosition(String message){
        //ask 2 integer to client
        return null;
    }

    static Dice askDice(GetDiceFrom from){
        // ask dice
        return  null;
    }

    static void putDice(GetDiceFrom to, Dice dice, Game game) throws RoundTrackFullException{
        switch (to){
            // TODO case BAG
            case DRAFT_POOL:
                game.getCurrentRound().setCurrentDiceDrafted(dice);
                break;
            case ROUND_TRACK:
                Dice[] roundTrack = game.getRoundTrack();
                int i;
                for(i=0;i<10;i++)  if(roundTrack[i] == null){
                        roundTrack[i] = dice;
                        break;
                    }
                if(i>10) throw new RoundTrackFullException();
                break;
            case BAG:
                game.putInBag(dice);
                break;
        }
    }

    static void move(Round round,RuleIgnored ruleIgnored){
        Coordinate start = null;
        Coordinate end = null;
        Window currentPlayerWindow = round.getCurrentPlayer().getWindow();
        Dice removedDice = null;
        boolean valid = false;
        String message = "Which dice do you want to move?";
        while (!valid){
            start = askPosition(message);
            message = "No dice there! Which dice do you want to move?";
            removedDice = currentPlayerWindow.getCell(start.getRow(),start.getColumn());
            if(removedDice != null) valid = true;
        }
        valid = false;
        message = "Where do you want to move it?";
        while (!valid) {
            end = askPosition(message);
            message = "Can't go there! Where do you want to move it?";
            if (start.getRow() == end.getRow() && start.getColumn() == end.getColumn()) continue;
            try {
                round.setCurrentDiceDrafted(removedDice);
                placeDice(round, end.getRow(), end.getColumn(), ruleIgnored);
                valid = true;
            } catch (NotWantedAdjacentDiceException e) {
                e.printStackTrace();
            } catch (ConstraintViolatedException e) {
                e.printStackTrace();
            } catch (NoAdjacentDiceException e) {
                e.printStackTrace();
            } catch (BadAdjacentDiceException e) {
                e.printStackTrace();
            } catch (FirstDiceMisplacedException e) {
                e.printStackTrace();
            }
        }
    }

    static void swap(Game game,GetDiceFrom from,GetDiceFrom to){
        Dice first = askDice(from);
        Dice second = askDice(to);
        try {
            putDice(to,first,game);
            putDice(from,second,game);
        } catch (RoundTrackFullException e) {
            e.printStackTrace();
        }
    }
}
