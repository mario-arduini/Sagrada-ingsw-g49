package it.polimi.ingsw.model;

public class Constraint {
    private final Color color;
    private final int number;

    public Constraint(Color color, int number){
        this.color = color;
        this.number = number;
    }

    public Color getColor(){
        return color;
    }

    public int getNumber(){
        return number;
    }
}
