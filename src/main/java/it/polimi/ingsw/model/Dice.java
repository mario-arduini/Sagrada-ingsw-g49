package it.polimi.ingsw.model;

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
        //Not implemented yet.
        return;
    }

    public void setValue(int value){
        this.value = value;
        return;
    }
}
