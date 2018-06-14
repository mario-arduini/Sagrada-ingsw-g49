package it.polimi.ingsw.network.client.model;

public class Dice {
    private final Color color;
    private int value;

    public Dice(Color color){
        this.color = color;
    }

    public Dice(Color color, int value) {
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

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object dice) {
        return dice instanceof Dice && this.color == ((Dice) dice).color && this.value == ((Dice) dice).value;
    }

    @Override
    public String toString(){
        switch (value){
            case 1: return color.escape()+"\u2680"+Color.RESET;
            case 2: return color.escape()+"\u2681"+Color.RESET;
            case 3: return color.escape()+"\u2682"+Color.RESET;
            case 4: return color.escape()+"\u2683"+Color.RESET;
            case 5: return color.escape()+"\u2684"+Color.RESET;
            case 6: return color.escape()+"\u2685"+Color.RESET;
            default: return color.escape()+"N/A"+Color.RESET;
        }
    }
}
