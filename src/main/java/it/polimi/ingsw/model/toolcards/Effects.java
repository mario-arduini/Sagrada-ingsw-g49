package it.polimi.ingsw.model.toolcards;

import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;

public final class Effects {

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

    
}
