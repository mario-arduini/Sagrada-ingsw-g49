package it.polimi.ingsw.model.toolcards;

import com.google.gson.JsonElement;
import it.polimi.ingsw.model.Dice;
import it.polimi.ingsw.model.Round;
import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;

public final class Effects {

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

    static void changeValue(Dice dice, boolean isPlus, int value) throws InvalidDiceValueException {
        if (isPlus) dice.setValue(dice.getValue()+value);
        else dice.setValue(dice.getValue()-value);
        return;
    }

    static void changeValue(Dice dice,int value) throws InvalidDiceValueException {
        dice.setValue(value);
        return;
    }
    
}
