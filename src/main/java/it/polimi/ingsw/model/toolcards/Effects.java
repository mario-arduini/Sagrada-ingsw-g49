package it.polimi.ingsw.model.toolcards;

import com.google.gson.JsonElement;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.BadAdjacentDiceException;
import it.polimi.ingsw.model.exceptions.ConstraintViolatedException;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import it.polimi.ingsw.model.exceptions.NoAdjacentDiceException;

import java.util.List;
import java.util.Random;

public final class Effects {

    enum RuleIgnored{ COLOR, NUMBER, ADJACENCIES }

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

    static void placeDice(Round round, int row, int column, RuleIgnored ruleIgnored) throws ConstraintViolatedException, NoAdjacentDiceException, BadAdjacentDiceException {
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
        }
    }

    static void removeTurn(Round round){
        List<Player> players = round.getPlayersOrder();
        for(int i = round.getCurrentPlayerIndex() + 1; i < players.size(); i++)
            if(players.get(i).getNickname().equals(round.getCurrentPlayer().getNickname()))
                round.getPlayersOrder().remove(i);
    }
}
