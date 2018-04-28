package it.polimi.ingsw.model;

import java.util.Random;

public class Dice {
    private final Color color;
    private int value;

    public Dice(Color color, int value){
        this.color = color;
        this.value = value;
    }

    public Color getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }

    protected void roll(){
        Random diceRoller = new Random();
        this.value = diceRoller.nextInt(6) + 1;
    }

    public void setValue(int value){
        this.value = value;
    }
}
