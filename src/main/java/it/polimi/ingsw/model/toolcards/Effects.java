package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.Window;
import it.polimi.ingsw.model.exceptions.*;

import java.util.List;
import java.util.Random;

public final class Effects {

    enum RuleIgnored{ COLOR, NUMBER, ADJACENCIES, NONE }

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

    static void placeDice(Round round, int row, int column, RuleIgnored ruleIgnored) throws ConstraintViolatedException, NoAdjacentDiceException, BadAdjacentDiceException, FirstDiceMisplacedException {
        switch (ruleIgnored){
            case COLOR:
                round.getCurrentPlayer().getWindow().checkValueConstraint(round.getCurrentPlayer().getWindow().getSchema().getConstraint(row, column), round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().checkAdjacencies(row, column, round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().setDice(row, column, round.getCurrentDiceDrafted());
                break;
            case NUMBER:
                round.getCurrentPlayer().getWindow().checkColorConstraint(round.getCurrentPlayer().getWindow().getSchema().getConstraint(row, column), round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().checkAdjacencies(row, column, round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().setDice(row, column, round.getCurrentDiceDrafted());
                break;
            case ADJACENCIES:
                round.getCurrentPlayer().getWindow().checkValueConstraint(round.getCurrentPlayer().getWindow().getSchema().getConstraint(row, column), round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().checkColorConstraint(round.getCurrentPlayer().getWindow().getSchema().getConstraint(row, column), round.getCurrentDiceDrafted());
                round.getCurrentPlayer().getWindow().setDice(row, column, round.getCurrentDiceDrafted());
                break;
            case NONE:
                round.getCurrentPlayer().getWindow().addDice(row, column, round.getCurrentDiceDrafted());
                break;
        }
    }

    static void exchange(List<Dice> draftPool, int draftPoolIndex, Dice[] roundTrack, int roundTrackIndex){
        Dice dice = draftPool.get(draftPoolIndex);
        draftPool.remove(draftPoolIndex);
        draftPool.add(roundTrack[roundTrackIndex]);
        roundTrack[roundTrackIndex] = new Dice(dice);
    }

    static void move(Round round){
        List<Integer> positions = null;
        // ask positions to client
        Window currentPlayerWindow = round.getCurrentPlayer().getWindow();
        Dice rem = currentPlayerWindow.getCell(positions.get(0),positions.get(1));
        currentPlayerWindow.removeDice(positions.get(0),positions.get(1));

    }
}
