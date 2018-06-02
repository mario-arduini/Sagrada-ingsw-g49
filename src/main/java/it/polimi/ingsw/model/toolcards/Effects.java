package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;

import java.util.List;
import java.util.Random;

public final class Effects {

    enum ConnectionType{ COLOR, NUMBER, ADJACENCIES }

    private Effects(){
        super();
    }

    public static void flip(Round round){
        Dice dice = round.getCurrentDiceDrafted();
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
}
