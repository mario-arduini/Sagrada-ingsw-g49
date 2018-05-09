package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;
import java.util.Random;

public class Dice {
    private final Color color;
    private int value;

    public Dice(Color color){
        this.color = color;
    }

    public Dice(Color color, int value) throws InvalidDiceValueException {
        if(value < 1 || value > 6)
            throw  new InvalidDiceValueException();
        this.color = color;
        this.value = value;
    }

    public Dice(Dice dice){
        this.value = dice.value;
        this.color = dice.color;
    }

    public Color getColor(){
        return color;
    }

    public int getValue(){
        return value;
    }

    public void setValue(int value) throws InvalidDiceValueException {
        if(value < 1 || value > 6)
            throw  new InvalidDiceValueException();
        this.value = value;
    }

    public void roll(){
        Random diceRoller = new Random();
        this.value = diceRoller.nextInt(6) + 1;
    }

    @Override
    public boolean equals(Object dice) {
        return dice instanceof Dice && this.color == ((Dice) dice).color && this.value == ((Dice) dice).value;
    }
}
