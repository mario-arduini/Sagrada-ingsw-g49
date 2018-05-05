package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.InvalidDiceValueException;

import java.util.Random;

public class Dice{
    private final Color color;
    private int value;

    public Dice(Color color){
        this.color = color;
    }

    public Dice(Color color, int value){
        this.color = color;
        this.value = value;
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

    protected void roll(){
        Random diceRoller = new Random();
        this.value = diceRoller.nextInt(6) + 1;
    }
}
